package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.traits.HasItems.items
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasPadding.padding
import com.anjunar.javafx.dsl.traits.IstTextInput.promptText
import com.anjunar.javafx.dsl.traits.HasSpacing.spacing
import com.anjunar.javafx.dsl.{ElementBuilder, Producer, Ref}
import com.anjunar.javafx.dsl.traits.HasText.text
import com.anjunar.javafx.dsl.traits.IsNode.{onMouseClicked, onMouseClicked_=, vgrow}
import com.anjunar.javafx.scene.control.comboBox.singleSelectionModel
import com.anjunar.javafx.scene.control.listView.cellFactory
import com.anjunar.javafx.scene.control.{button, comboBox, label, listView, passwordField, textField}
import com.anjunar.javafx.scene.layout.{hbox, vbox}
import com.anjunar.javafx.scene.window
import com.anjunar.javafx.scene.window.{close, closeWithResult}
import com.anjunar.javafx.stage.Window
import com.anjunar.jcommander.ConfigDir
import com.anjunar.jcommander.components.VFS2ClientComponent.Connection
import com.anjunar.jcommander.configuration.SFTPConnection
import com.anjunar.jcommander.security.PasswordStore
import com.anjunar.jcommander.utils.FileSystemManagerBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import javafx.beans.property.SimpleListProperty
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.control
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.geometry.Insets
import javafx.scene.control.{ListCell, ListView, SingleSelectionModel}
import javafx.scene.input.MouseButton
import javafx.scene.layout.Priority
import javafx.util.Callback

import java.io.File
import java.nio.file.Files
import scala.jdk.CollectionConverters.*

class VFS2Client extends ElementBuilder[Window[Connection]] {

  private val manager = FileSystemManagerBuilder.build()

  private val configDir = ConfigDir.path()

  private val keyPath = new File(configDir, "master.key")
  private val passPath = new File(configDir, "passwords.json")
  private val connPath = new File(configDir, "connections.json")

  private val store = new PasswordStore(passPath.toPath, keyPath.toPath)
  private val mapper = new ObjectMapper().registerModule(DefaultScalaModule)

  private val connRef = Ref[comboBox[String]]()
  private val hostRef = Ref[textField]()
  private val portRef = Ref[textField]()
  private val userRef = Ref[textField]()
  private val passRef = Ref[passwordField]()
  private val listRef = Ref[listView[SFTPConnection]]()

  private val statusRef = Ref[label]()

  private def loadConnections(): ObservableList[SFTPConnection] = {
    if (!Files.exists(connPath.toPath)) return FXCollections.observableArrayList[SFTPConnection]()
    val json = mapper.readValue(connPath, classOf[Array[SFTPConnection]])
    FXCollections.observableArrayList[SFTPConnection](json.toSeq: _*)
  }

  private def saveConnections(conns: Seq[SFTPConnection]): Unit = {
    Files.createDirectories(connPath.toPath.getParent)
    mapper.writerWithDefaultPrettyPrinter().writeValue(connPath, conns.toArray)
  }

  private val connections = loadConnections()

  lazy val node : Window[Connection] = component[Window[Connection]] {
    window[Connection]() {
      vbox() {
        padding = new Insets(10)
        hbox() {
          spacing = 10
          vbox() {
            spacing = 10
            comboBox[String](connRef) {
              items = FXCollections.observableArrayList("ftp", "sftp")
              singleSelectionModel((model : SingleSelectionModel[String]) => {
                model.selectedItemProperty().addListener(new ChangeListener[String] {
                  override def changed(observableValue: ObservableValue[_ <: String], t: String, t1: String): Unit =
                    t1 match {
                      case "ftp" => portRef { text = "21" }
                      case "sftp" => portRef { text = "22" }
                    }
                })
              })
            }
            textField(hostRef) {
              promptText = "Host"
            }
            textField(portRef) {
              promptText = "Port"
              text = "22"
            }
            textField(userRef) {
              promptText = "Username"
            }
            passwordField(passRef) {
              promptText = "Password"
            }
            hbox() {
              spacing = 10
              button() {
                text = "Connect"
                onAction = _ => {
                  val host = hostRef.get.node.getText
                  val port = try portRef.get.node.getText.toInt catch {
                    case _: Throwable => 22
                  }
                  val connType = connRef.get.node.getValue
                  val user = userRef.get.node.getText
                  val pass = passRef.get.node.getText

                  if (host == null || host.trim.isEmpty) {
                    statusRef.get.node.setText("Host required")
                  }

                  if (pass != null && pass.nonEmpty) store.savePassword(host, pass)

                  val task = new Task[Connection] {
                    override def call(): Connection = {
                      val uri = s"$connType://$user:$pass@$host:$port/"
                      val remoteFile = manager.resolveFile(uri)
                      if (!remoteFile.exists()) throw new Exception("Connection failed or directory empty")
                      Connection(uri, manager)
                    }
                  }

                  task.setOnSucceeded(_ => {
                    statusRef.get.node.setText("Connected successfully!")
                    closeWithResult(task.get())
                  })

                  task.setOnFailed(_ => {
                    statusRef.get.node.setText(s"Error: ${task.getException.getMessage}")
                  })

                  new Thread(task).start()
                }
              }
              button() {
                text = "Save"
                onAction = _ => {
                  val host = hostRef.get.node.getText
                  val port = try portRef.get.node.getText.toInt catch {
                    case _: Throwable => 22
                  }
                  val connType = connRef.get.node.getValue
                  val user = userRef.get.node.getText
                  val pass = passRef.get.node.getText

                  if (pass != null && pass.nonEmpty) store.savePassword(host, pass)

                  val entry = SFTPConnection(connType, host, port, user, null)
                  val existing = connections.asScala.filterNot(_.host == host)
                  connections.setAll((existing.toSeq :+ entry): _*)
                  saveConnections(connections.asScala.toSeq)
                }
              }
              button() {
                text = "Delete"
                onAction = _ => {
                  val sel = listRef.get.node.getSelectionModel.getSelectedItem
                  if (sel != null) {
                    connections.remove(sel)
                    saveConnections(connections.asScala.toSeq)
                  }
                }
              }
            }
          }
          listView[SFTPConnection](listRef) {
            items = connections
            cellFactory = new Callback[ListView[SFTPConnection], ListCell[SFTPConnection]] {
              override def call(v: ListView[SFTPConnection]): ListCell[SFTPConnection] =
                new ListCell[SFTPConnection] {
                  override def updateItem(item: SFTPConnection, empty: Boolean): Unit = {
                    super.updateItem(item, empty)
                    if (empty || item == null) setText(null)
                    else setText(s"${item.connectionType}:///${item.host}:${item.port} – ${item.username}")
                  }
                }
            }
            onMouseClicked = { e =>
              if (e.getButton == MouseButton.PRIMARY && e.getClickCount == 2) {
                val sel = listRef.get.node.getSelectionModel.getSelectedItem
                if (sel != null) {
                  connRef.get.node.getSelectionModel.select(sel.connectionType)
                  hostRef.get.node.setText(sel.host)
                  portRef.get.node.setText(sel.port.toString)
                  userRef.get.node.setText(sel.username)
                  val p = store.loadPassword(sel.host)
                  if (p != null) passRef.get.node.setText(p) else passRef.get.node.clear()
                }
              }
            } 
          }
        }
        label(statusRef) {
          vgrow = Priority.ALWAYS
          text = "Not Connected"
        }
      }
    }
  }

  override def build(): Window[Connection] = node

}

object VFS2Client extends Producer[VFS2Client, Window[Connection]] {
  override def createBuilder: VFS2Client = new VFS2Client()
}

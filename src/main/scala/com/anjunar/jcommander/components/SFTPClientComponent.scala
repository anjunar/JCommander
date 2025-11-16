package com.anjunar.jcommander.components

import com.anjunar.jcommander.configuration.SFTPConnection
import com.anjunar.jcommander.security.PasswordStore
import com.anjunar.jcommander.ui.ThemedDialog
import com.anjunar.jcommander.utils.FileSystemManagerBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import javafx.concurrent.Task
import javafx.scene.control
import javafx.scene.input.MouseButton
import javafx.util.Callback
import org.apache.commons.vfs2.{FileObject, FileSystemManager}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.*
import scalafx.scene.layout.*

import java.nio.file.{Files, Paths}

class SFTPClientComponent extends Component[ThemedDialog[FileSystemManager]] {

  val manager = FileSystemManagerBuilder.build()
  var base: FileObject = _

  val keyPath = Paths.get(sys.env("LOCALAPPDATA"), "jcommander", "master.key")
  val passPath = Paths.get(sys.env("LOCALAPPDATA"), "jcommander", "passwords.json")
  val connPath = Paths.get(sys.env("LOCALAPPDATA"), "jcommander", "connections.json")

  val store = new PasswordStore(passPath, keyPath)
  val mapper = new ObjectMapper().registerModule(DefaultScalaModule)

  def resolve(path: String): FileObject =
    manager.resolveFile(base, path)

  def loadConnections(): ObservableBuffer[SFTPConnection] = {
    if (!Files.exists(connPath)) return ObservableBuffer.empty[SFTPConnection]
    val json = mapper.readValue(connPath.toFile, classOf[Array[SFTPConnection]])
    ObservableBuffer(json.toSeq: _*)
  }

  def saveConnections(conns: Seq[SFTPConnection]): Unit = {
    Files.createDirectories(connPath.getParent)
    mapper.writerWithDefaultPrettyPrinter().writeValue(connPath.toFile, conns.toArray)
  }

  override val node: ThemedDialog[FileSystemManager] = new ThemedDialog[FileSystemManager] {

    val connections = loadConnections()

    private val hostField = new TextField {
      promptText = "Host"
    }
    private val portField = new TextField {
      promptText = "Port"; text = "22"
    }
    private val userField = new TextField {
      promptText = "Username"
    }
    private val passField = new PasswordField {
      promptText = "Password"
    }

    private val connectButton = new Button("Connect")
    private val saveButton = new Button("Save")
    private val deleteButton = new Button("Delete")
    private val statusLabel = new Label("Not connected")

    private val listView = new ListView[SFTPConnection](connections) {
      cellFactory = new Callback[control.ListView[SFTPConnection], control.ListCell[SFTPConnection]] {
        override def call(v: javafx.scene.control.ListView[com.anjunar.jcommander.configuration.SFTPConnection]): control.ListCell[SFTPConnection] =
          new javafx.scene.control.ListCell[SFTPConnection] {
            override def updateItem(item: SFTPConnection, empty: Boolean): Unit = {
              super.updateItem(item, empty)
              if (empty || item == null) setText(null)
              else setText(s"${item.host}:${item.port} – ${item.username}")
            }
          }
      }
    }


    title = "SFTP Client"

    listView.setOnMouseClicked { e =>
      if (e.getButton == MouseButton.PRIMARY && e.getClickCount == 2) {
        val sel = listView.selectionModel().getSelectedItem
        if (sel != null) {
          hostField.text = sel.host
          portField.text = sel.port.toString
          userField.text = sel.username
          val p = store.loadPassword(sel.host)
          if (p != null) passField.text = p else passField.clear()
        }
      }
    }

    saveButton.setOnAction(_ => {
      val host = hostField.text.value
      val port = try portField.text.value.toInt catch {
        case _: Throwable => 22
      }
      val user = userField.text.value
      val pass = passField.text.value

      if (pass != null && pass.nonEmpty) store.savePassword(host, pass)

      val entry = SFTPConnection(host, port, user, null)
      val existing = connections.filterNot(_.host == host)
      connections.setAll((existing.toSeq :+ entry): _*)
      saveConnections(connections.toSeq)
    })

    deleteButton.setOnAction(_ => {
      val sel = listView.selectionModel().getSelectedItem
      if (sel != null) {
        connections.remove(sel)
        saveConnections(connections.toSeq)
      }
    })

    dialogPane.content = new HBox {
      spacing = 10
      children = Seq(
        new VBox(10) {
          children = Seq(
            hostField,
            portField,
            userField,
            passField,
            new HBox(10) {
              children = Seq(connectButton, saveButton, deleteButton)
            },
            statusLabel
          )
          VBox.setVgrow(statusLabel, Priority.Always)
        },
        listView
      )
    }

    connectButton.setOnAction(_ => {
      val host = hostField.text.value
      val port = try portField.text.value.toInt catch {
        case _: Throwable => 22
      }
      val user = userField.text.value
      val pass = passField.text.value

      if (host == null || host.trim.isEmpty) {
        statusLabel.text = "Host required"
      }

      if (pass != null && pass.nonEmpty) store.savePassword(host, pass)

      val task = new Task[FileSystemManager] {
        override def call(): FileSystemManager = {
          val uri = s"sftp://$user:$pass@$host:$port/"
          val remoteFile = manager.resolveFile(uri)
          if (!remoteFile.exists()) throw new Exception("Connection failed or directory empty")
          manager
        }
      }

      task.setOnSucceeded(_ => {
        statusLabel.text = "Connected successfully!"
        setResult(task.get())
        close()
      })

      task.setOnFailed(_ => {
        statusLabel.text = s"Error: ${task.getException.getMessage}"
      })

      new Thread(task).start()
    })
  }
}

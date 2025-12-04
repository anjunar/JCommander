package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.ChildBuilder.{reactTo, reactTo as content}
import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.traits.IsNode.vgrow
import com.anjunar.javafx.dsl.traits.HasSpacing.alignment
import com.anjunar.javafx.dsl.traits.HasText.text
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasPadding.padding
import com.anjunar.javafx.dsl.traits.HasStyle.style
import com.anjunar.javafx.dsl.{BuildContext, ChildNodeBuilder, ElementBuilder, NodeBuilder, Producer, Ref}
import com.anjunar.javafx.scene.control.seperator.orientation
import com.anjunar.javafx.scene.control.{button, seperator}
import com.anjunar.javafx.scene.layout.{hbox, vbox}
import com.anjunar.javafx.stage.Window
import com.anjunar.jcommander.components.VFS2ClientComponent.Connection
import com.anjunar.jcommander.configuration.SFTPConnection
import com.anjunar.jcommander.dsl.BreadCrumb.table
import com.anjunar.jcommander.dsl.DriveButtons.*
import com.anjunar.jcommander.dsl.traits.HasDirectory
import com.anjunar.jcommander.dsl.traits.HasDirectory.{directory, directoryProp, directory_=}
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.geometry.{Insets, Orientation, Pos}
import javafx.scene.layout.{Priority, VBox}
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections

import java.io.File
import scala.compiletime.uninitialized

class FilePane extends NodeBuilder[VBox] {

  private val fileTableRef = Ref[LocalFileTable | VFS2FileTable]()

  private val breadCrumbRef = Ref[BreadCrumb]()

  private var onTableChange : FileTable => Unit = uninitialized

  private val content = FXCollections.observableArrayList[FileTable & ElementBuilder[?]]()

  lazy val node : VBox = {
    val filePane = component[VBox] {
      vbox() {
        vgrow = Priority.ALWAYS

        hbox() {
          padding = new Insets(10)
          borderPaneAlignment = Pos.CENTER_LEFT

          DriveButtons() {
            change = (file: File) => {
              fileTableRef.get match {
                case vfs2FileTable: VFS2FileTable =>
                  content.clear()
                  content.add(LocalFileTable.build() {
                    vgrow = Priority.ALWAYS
                    directory = file.getAbsolutePath
                    directoryProp((prop: StringProperty) => {
                      prop.addListener((_, _, newValue) => {
                        breadCrumbRef {
                          directory = newValue
                        }
                      })
                    })
                  })
                case _ =>
                  fileTableRef {
                    directory = file.getAbsolutePath
                  }
              }
            }
            unmount = drive => {
              fileTableRef {
                directory = System.getProperty("user.home")
              }
            }
          }

          seperator() {
            orientation = Orientation.VERTICAL
          }

          button() {
            text = "VSF2"
            style = "-fx-background-color: transparent;" +
              "-fx-border-color: transparent;" +
              "-fx-padding: 0;" +
              "-fx-focus-color: transparent;" +
              "-fx-faint-focus-color: transparent;"

            onAction = _ => {
              val client = component[Window[Connection]] {
                VFS2Client() {}
              }

              client.showAndWaitResult().foreach(connection => {
                content.clear()
                content.add(VFS2FileTable.build(connection, fileTableRef.asInstanceOf[Ref[VFS2FileTable]]) {
                  vgrow = Priority.ALWAYS
                  directory = connection.url
                  directoryProp((prop: StringProperty) => {
                    prop.addListener((_, _, newValue) => {
                      breadCrumbRef {
                        directory = newValue
                      }
                    })
                  })
                })
              })
            }
          }
        }

        BreadCrumb(breadCrumbRef) {
          padding = new Insets(0, 0, 10, 10)
          table = fileTableRef
          directory = System.getProperty("user.home")
        }

        vbox() {
          vgrow = Priority.ALWAYS

          content.add(LocalFileTable.build(fileTableRef.asInstanceOf[Ref[LocalFileTable]]) {
            vgrow = Priority.ALWAYS
            directory = System.getProperty("user.home")
            directoryProp((prop: StringProperty) => {
              prop.addListener((_, _, newValue) => {
                breadCrumbRef {
                  directory = newValue
                }
              })
            })
          })

          reactTo(content)
        }


      }
    }
    
    filePane
  }

  override def build(): VBox = node

  override def afterBuild(): Unit = onTableChange(fileTableRef.get)

}

object FilePane extends Producer[FilePane, VBox] {

  override def createBuilder: FilePane = new FilePane

  def onTableChange()(using h: FilePane & ElementBuilder[?], ctx : BuildContext): FileTable => Unit =
    h.read(h.onTableChange)

  def onTableChange_=(v: FileTable => Unit)(using h: FilePane & ElementBuilder[?], ctx : BuildContext): Unit =
    h.write(() => h.onTableChange = v)

}

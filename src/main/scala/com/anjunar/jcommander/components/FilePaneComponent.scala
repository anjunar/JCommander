package com.anjunar.jcommander.components

import com.anjunar.jcommander.CdiUtils.*
import com.anjunar.jcommander.manager.FileTableManager
import jakarta.enterprise.context.ApplicationScoped
import javafx.scene.control
import org.apache.commons.vfs2.FileSystemManager
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Separator, TableView}
import scalafx.scene.layout.{HBox, Priority, VBox}

import java.io.File
import scalafx.application.Platform

class FilePaneComponent(position : String, newTable : AbstractFileTableComponent => Unit ) extends Component[VBox] {

  val fileTableManager = inject(classOf[FileTableManager])

  val driveButtons : DriveButtonsComponent = new DriveButtonsComponent(drive => {
    table match {
      case local: LocalFileTableComponent => local.loadDirectory(drive.getAbsolutePath)
      case _ =>
        table = new LocalFileTableComponent
        table.loadDirectory(drive.getAbsolutePath)
        newTable(table)
        VBox.setVgrow(table.node, Priority.Always)
        node.children.set(1, table.node)

        table.node.items.onChange {
          if (table.directory != null)
            Platform.runLater(() => updateBreadcrumb(table.directory))
        }
    }
  })

  var table: AbstractFileTableComponent = new LocalFileTableComponent

  position match {
    case "left" => fileTableManager.loadLeft(table)
    case "right" => fileTableManager.loadRight(table)
  }

  val breadcrumbBox = new HBox {
    spacing = 5

    table.node.items.onChange {
      if (table.directory != null)
        Platform.runLater(() => updateBreadcrumb(table.directory))
    }
  }

  val node: VBox = new VBox {
    spacing = 6
    padding = Insets(6)
    children = Seq(
      new HBox {
        spacing = 5
        children = Seq(
          driveButtons.node,
          new Separator() { orientation = scalafx.geometry.Orientation.Vertical },
          new Button("SFTP") {
            onAction = _ => {
              new SFTPClientComponent().node.showAndWaitDialog().foreach(manager => {
                table = new SFTPFileTableComponent(manager)
                table.loadDirectory("sftp://patrick:cubase@patricks-mbp.fritz.box/")
                newTable(table)
                VBox.setVgrow(table.node, Priority.Always)
                node.children.set(1, table.node)

                table.node.items.onChange {
                  if (table.directory != null)
                    Platform.runLater(() => updateBreadcrumb(table.directory))
                }
              })
            }
          },
          new Separator() { orientation = scalafx.geometry.Orientation.Vertical },
          breadcrumbBox
        )
      },
      table.node
    )
    VBox.setVgrow(table.node, Priority.Always)
    table.node.maxHeight = Double.MaxValue
    maxHeight = Double.MaxValue
  }

  def updateBreadcrumb(dir: String): Unit = {
    breadcrumbBox.children.clear()

    val parts = getPathParts(new File(dir))
    for ((part, idx) <- parts.zipWithIndex) {
      val btn = new Button(part.getName match {
        case "" => part.getAbsolutePath // Root anzeigen, z. B. "C:\"
        case n  => n
      }) {
        onAction = _ => table.loadDirectory(part.getAbsolutePath)
        style = "-fx-background-color: transparent; -fx-text-fill: -fx-text-base-color; -fx-underline: true;"
      }

      breadcrumbBox.children += btn
    }
  }

  private def getPathParts(dir: File): Seq[File] = {
    var parts = List(dir)
    var parent = dir.getParentFile
    while (parent != null) {
      parts = parent :: parts
      parent = parent.getParentFile
    }
    parts
  }

}
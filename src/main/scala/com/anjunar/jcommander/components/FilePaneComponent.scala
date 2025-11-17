package com.anjunar.jcommander.components

import com.anjunar.jcommander.utils.CdiUtils.*
import com.anjunar.jcommander.manager.FileTableManager
import com.anjunar.jcommander.utils.FileSystemManagerBuilder
import jakarta.enterprise.context.ApplicationScoped
import javafx.scene.control
import org.apache.commons.vfs2.{FileObject, FileSystemManager}
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Separator, TableView}
import scalafx.scene.layout.{HBox, Priority, VBox}
import scalafx.application.Platform

class FilePaneComponent(position: String, newTable: AbstractFileTableComponent => Unit) extends Component[VBox] {

  val fileTableManager = inject(classOf[FileTableManager])

  var table: AbstractFileTableComponent =
    new LocalFileTableComponent(FileSystemManagerBuilder.build())

  table.onDirectoryChanged = Some(dir => Platform.runLater(() => updateBreadcrumb(dir)))

  position match {
    case "left"  => fileTableManager.loadLeft(table)
    case "right" => fileTableManager.loadRight(table, true)
  }

  val driveButtons: DriveButtonsComponent =
    new DriveButtonsComponent(drive => {
      table match {
        case local: LocalFileTableComponent =>
          local.loadDirectory(drive.getAbsolutePath)
        case _ =>
          table = new LocalFileTableComponent(FileSystemManagerBuilder.build())
          table.onDirectoryChanged = Some(dir => Platform.runLater(() => updateBreadcrumb(dir)))
          table.loadDirectory(drive.getAbsolutePath)
          newTable(table)
          VBox.setVgrow(table.node, Priority.Always)
          node.children.set(2, table.node)
      }
    })

  val breadcrumbBox = new HBox {
    style = "-fx-background-color: -fx-table-cell-border-color; -fx-padding: 0; -fx-alignment: CENTER_LEFT;"
    minHeight = 14
    prefHeight = 14
    maxHeight = 14
    spacing = 2
  }

  val node: VBox = new VBox {
    spacing = 6
    padding = Insets(6)
    children = Seq(
      new HBox {
        spacing = 5
        children = Seq(
          driveButtons.node,
          new Separator { orientation = scalafx.geometry.Orientation.Vertical },
          new Button("VFS2") {
            onAction = _ => {
              new VFS2ClientComponent().node.showAndWaitDialog().foreach { connection =>
                table = new VFS2FileTableComponent(connection.manager)
                table.onDirectoryChanged = Some(dir => updateBreadcrumb(dir))
                table.loadDirectory(connection.url)
                newTable(table)
                VBox.setVgrow(table.node, Priority.Always)
                node.children.set(2, table.node)
              }
            }
          },
          new Separator { orientation = scalafx.geometry.Orientation.Vertical },

        )
      },
      breadcrumbBox,
      table.node
    )
    VBox.setVgrow(table.node, Priority.Always)
    table.node.maxHeight = Double.MaxValue
    maxHeight = Double.MaxValue
  }

  def updateBreadcrumb(dir: String): Unit = {
    breadcrumbBox.children.clear()
    val current: FileObject = table.manager.resolveFile(dir)
    val parts = getVfsPathParts(current)

    for (fileObj <- parts) {
      val label =
        if (fileObj.getName.getBaseName.isEmpty)
          fileObj.getName.getRoot.getFriendlyURI
        else
          fileObj.getName.getBaseName

      val btn = new Button(label.replaceFirst("file:///", "") + " /") {
        style =
          "-fx-background-color: transparent;" +
            "-fx-border-width: 0;" +
            "-fx-padding: 0;" +
            "-fx-text-fill: -fx-text-base-color;" +
            "-fx-opacity: 0.85;"
        minHeight = 14
        prefHeight = 14
        maxHeight = 14
        onAction = _ => table.loadDirectory(fileObj.getName.getURI)
      }

      breadcrumbBox.children += btn
    }
  }

  private def getVfsPathParts(file: FileObject): Seq[FileObject] = {
    var parts = List(file)
    var parent = file.getParent

    while (parent != null) {
      parts = parent :: parts
      parent = parent.getParent
    }

    parts
  }
}

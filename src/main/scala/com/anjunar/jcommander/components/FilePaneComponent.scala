package com.anjunar.jcommander.components

import com.anjunar.jcommander.configuration.FileTableConf
import com.anjunar.jcommander.manager.FileTableManager
import com.anjunar.jcommander.utils.CdiUtils.*
import com.anjunar.jcommander.utils.FileSystemManagerBuilder
import org.apache.commons.vfs2.FileObject
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Separator, TableView}
import scalafx.scene.layout.{HBox, Priority, VBox}

import java.io.File

class FilePaneComponent(position: String, newTable: AbstractFileTableComponent => Unit) extends Component[VBox] {

  var table: AbstractFileTableComponent = new LocalFileTableComponent(FileSystemManagerBuilder.build())

  private val fileTableManager = inject(classOf[FileTableManager])

  private val leftConf = inject(classOf[FileTableConf.Left])
  private val rightConf = inject(classOf[FileTableConf.Right])

  table.onDirectoryChanged = Some(dir => Platform.runLater(() => updateBreadcrumb(dir)))

  position match {
    case "left" =>
//      fileTableManager.loadLeft(table)
      table.loadDirectory(leftConf.file.getAbsolutePath)
    case "right" =>
//      fileTableManager.loadRight(table, true)
      table.loadDirectory(rightConf.file.getAbsolutePath)
  }

  val driveButtons: DriveButtonsComponent =
    new DriveButtonsComponent((drive : File) => {
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
    }, unmounted => {
      if (unmounted.file.getAbsolutePath.startsWith(table.directory.substring(0, 2))) {
        Platform.runLater({
          table = new LocalFileTableComponent(FileSystemManagerBuilder.build())
          table.onDirectoryChanged = Some(dir => Platform.runLater(() => updateBreadcrumb(dir)))
          table.loadDirectory(System.getProperty("user.home"))
          newTable(table)
          VBox.setVgrow(table.node, Priority.Always)
          node.children.set(2, table.node)
        })
      }
    })

  val breadcrumbBox = new HBox {
    style = "-fx-background-color: -fx-table-cell-border-color; -fx-padding: 0 0 0 5 ; -fx-alignment: CENTER_LEFT;"
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
          new Separator {
            orientation = scalafx.geometry.Orientation.Vertical
          },
          new Button("VFS2") {
            style = "-fx-background-color: transparent;" +
              "-fx-border-color: transparent;" +
              "-fx-padding: 0;" +
              "-fx-focus-color: transparent;" +
              "-fx-faint-focus-color: transparent;"

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
          }
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

      val btn = new Button(label.replaceFirst("file:///", "").replaceAll("/", "") + "/") {
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

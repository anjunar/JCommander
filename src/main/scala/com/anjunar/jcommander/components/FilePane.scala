package com.anjunar.jcommander.components

import com.anjunar.jcommander.inject
import jakarta.enterprise.context.ApplicationScoped
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Separator}
import scalafx.scene.layout.{HBox, Priority, VBox}
import java.io.File
import scalafx.application.Platform

abstract class FilePane extends Component[VBox] {

  val driveButtons : DriveButtons
  val table: FileTable

  lazy val breadcrumbBox = new HBox {
    spacing = 5

    table.node.items.onChange {
      if (table.directory != null)
        Platform.runLater(() => updateBreadcrumb(table.directory))
    }
  }

  lazy val node: VBox = new VBox {
    spacing = 6
    padding = Insets(6)
    children = Seq(
      new HBox {
        spacing = 5
        children = Seq(driveButtons.node, new Separator() { orientation = scalafx.geometry.Orientation.Vertical },  breadcrumbBox)
      },
      table.node
    )
    VBox.setVgrow(table.node, Priority.Always)
    table.node.maxHeight = Double.MaxValue
    maxHeight = Double.MaxValue
  }

  def updateBreadcrumb(dir: File): Unit = {
    breadcrumbBox.children.clear()

    val parts = getPathParts(dir)
    for ((part, idx) <- parts.zipWithIndex) {
      val btn = new Button(part.getName match {
        case "" => part.getAbsolutePath // Root anzeigen, z. B. "C:\"
        case n  => n
      }) {
        onAction = _ => table.loadDirectory(part)
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

object FilePane {

  @ApplicationScoped
  class Left extends FilePane {
    val driveButtons: DriveButtons = inject(classOf[DriveButtons.Left])
    val table: FileTable = inject(classOf[FileTable.Left])
  }

  @ApplicationScoped
  class Right extends FilePane {
    val driveButtons: DriveButtons = inject(classOf[DriveButtons.Right])
    val table: FileTable = inject(classOf[FileTable.Right])
  }
}

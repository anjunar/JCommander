package com.anjunar.jcommander.components

import com.anjunar.jcommander.{Component, inject}
import jakarta.enterprise.context.ApplicationScoped
import scalafx.geometry.Insets
import scalafx.scene.layout.{HBox, Priority, VBox}

abstract class FilePane extends Component[VBox] {

  val driveButtons = inject(classOf[DriveButtons])
  
  val table: FileTable
  
  lazy val node = new VBox {
    spacing = 6
    padding = Insets(6)
    children = Seq(
      new HBox {
        spacing = 5
        children = Seq(
          driveButtons.node
        )
      },
      table.node
    )
    VBox.setVgrow(table.node, Priority.Always)
    table.node.maxHeight = Double.MaxValue
    maxHeight = Double.MaxValue
  }

}

object FilePane {

  @ApplicationScoped
  class Left extends FilePane {
    val table: FileTable = inject(classOf[FileTable.Left])  
  }
  
  @ApplicationScoped
  class Right extends FilePane {
    val table: FileTable = inject(classOf[FileTable.Right])
  } 

}

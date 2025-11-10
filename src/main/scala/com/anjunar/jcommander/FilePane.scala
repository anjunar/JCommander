package com.anjunar.jcommander

import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Label, TableView, TextField}
import scalafx.scene.layout.{HBox, Priority, VBox}

import java.io.File
import java.nio.file.FileStore

class FilePane(table: TableView[FileItem], openAction: (store : FileStore) => Unit) extends VBox {

  spacing = 6
  padding = Insets(6)
  children = Seq(
    new HBox {
      spacing = 5
      children = Seq(
        new DriveButtons(openAction)
          .buttonBox
      )
    },
    table
  )
  VBox.setVgrow(table, Priority.Always)
  table.maxHeight = Double.MaxValue
  maxHeight = Double.MaxValue

}

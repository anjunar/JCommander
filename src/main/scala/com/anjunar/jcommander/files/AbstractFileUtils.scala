package com.anjunar.jcommander.files

import com.anjunar.jcommander.FileTable
import scalafx.Includes.{jfxDialogPane2sfx, observableList2ObservableBuffer}
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.scene.control.*
import scalafx.scene.layout.VBox

import java.io.File
import java.nio.file.Files

abstract class AbstractFileUtils extends FileUtils {

  def mkDir(activeTable: ObjectProperty[FileTable], darkMode: BooleanProperty): Unit = {
    val textField: TextField = new TextField {
      promptText = "Directory Name"
    }

    val mkDirDialog = new Dialog[ButtonType]() {
      title = "Create Directory"
      headerText = "Create Directory"
      dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
      dialogPane().content = new VBox(10, textField)
      dialogPane().getStylesheets.add(
        getClass.getResource(s"/${if darkMode.value then "dark" else "light"}-theme.css").toExternalForm
      )
    }

    mkDirDialog.resultConverter = btn => btn

    mkDirDialog.showAndWait().foreach { result =>
      if (result == ButtonType.OK) {
        val newFileName = textField.text.value
        Files.createDirectory(activeTable.value.directory.toPath.resolve(newFileName))
      }
    }
  }

  def renameFile(activeTable: ObjectProperty[FileTable], darkMode: BooleanProperty): Unit = {
    val textField: TextField = new TextField {
      text = activeTable.value.selectionModel.value.getSelectedItem.name
    }

    val renameDialog = new Dialog[ButtonType]() {
      title = "Rename File"
      headerText = "Rename File"
      dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
      dialogPane().content = new VBox(10, textField)
      dialogPane().getStylesheets.add(
        getClass.getResource(s"/${if darkMode.value then "dark" else "light"}-theme.css").toExternalForm
      )
    }

    renameDialog.resultConverter = btn => btn

    renameDialog.showAndWait().foreach { result =>
      if (result == ButtonType.OK) {
        val newFileName = textField.text.value
        val oldPath = activeTable.value.selectionModel.value.getSelectedItems.head.file.toPath
        val newPath = oldPath.getParent.resolve(newFileName)
        Files.move(oldPath, newPath)
      }
    }
  }


}

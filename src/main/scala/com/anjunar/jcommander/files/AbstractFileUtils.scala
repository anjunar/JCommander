package com.anjunar.jcommander.files

import com.anjunar.jcommander.components.{DarkModeComponent, FileTableComponent}
import com.anjunar.jcommander.CdiUtils.*
import com.typesafe.scalalogging.Logger
import scalafx.Includes.{jfxDialogPane2sfx, observableList2ObservableBuffer}
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.scene.control.*
import scalafx.scene.layout.VBox

import java.io.File
import java.nio.file.Files
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class AbstractFileUtils extends FileUtils {
  
  val log = Logger[AbstractFileUtils]
  
  val darkMode = inject(classOf[DarkModeComponent])

  override def executeFile(file: File, workingDir : File, args: Seq[String]): Unit = {
    Future {
      try {
        val pb = new ProcessBuilder(Seq(file.getAbsolutePath) ++ args *)
        if (workingDir != null) 
          pb.directory(workingDir)
        val p = pb.start()
        val code = p.waitFor()
      } catch {
        case ex => log.error(ex.getMessage, ex)
      }
    }
  }

  def mkDir(activeTable: FileTableComponent): Unit = {
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
        Files.createDirectory(activeTable.directory.toPath.resolve(newFileName))
      }
    }
  }

  def renameFile(activeTable: FileTableComponent): Unit = {
    val textField: TextField = new TextField {
      text = activeTable.node.selectionModel.value.getSelectedItem.name
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
        val oldPath = activeTable.node.selectionModel.value.getSelectedItems.head.file.toPath
        val newPath = oldPath.getParent.resolve(newFileName)
        Files.move(oldPath, newPath)
      }
    }
  }


}

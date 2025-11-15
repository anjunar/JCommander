package com.anjunar.jcommander.files

import com.anjunar.jcommander.components.{DarkModeComponent, AbstractFileTableComponent}
import com.anjunar.jcommander.CdiUtils.*
import com.anjunar.jcommander.ui.ThemedDialog
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

  override def executeFile(file: String, workingDir : String, args: Seq[String]): Unit = {
    Future {
      try {
        val pb = new ProcessBuilder(Seq(file) ++ args *)
        if (workingDir != null) 
          pb.directory(new File(workingDir))
        val p = pb.start()
        val code = p.waitFor()
      } catch {
        case ex => log.error(ex.getMessage, ex)
      }
    }
  }

  def mkDir(activeTable: AbstractFileTableComponent): Unit = {
    val textField: TextField = new TextField {
      promptText = "Directory Name"
    }

    val mkDirDialog = new ThemedDialog[ButtonType]() {
      title = "Create Directory"
      headerText = "Create Directory"
      dialogPane.buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
      dialogPane.content = new VBox(10, textField)
    }

    mkDirDialog.resultConverter = btn => btn

    mkDirDialog.showAndWaitDialog().foreach { result =>
      if (result == ButtonType.OK) {
        val newFileName = textField.text.value
        Files.createDirectory(new File(activeTable.directory).toPath.resolve(newFileName))
      }
    }
  }

  def renameFile(activeTable: AbstractFileTableComponent): Unit = {
    val textField: TextField = new TextField {
      text = activeTable.node.selectionModel.value.getSelectedItem.name
    }

    val renameDialog = new ThemedDialog[ButtonType]() {
      title = "Rename File"
      headerText = "Rename File"
      dialogPane.buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
      dialogPane.content = new VBox(10, textField)
    }

    renameDialog.resultConverter = btn => btn

    renameDialog.showAndWaitDialog().foreach { result =>
      if (result == ButtonType.OK) {
        val newFileName = textField.text.value
        val oldPath = activeTable.node.selectionModel.value.getSelectedItems.head.asJavaFile.toPath
        val newPath = oldPath.getParent.resolve(newFileName)
        Files.move(oldPath, newPath)
      }
    }
  }


}

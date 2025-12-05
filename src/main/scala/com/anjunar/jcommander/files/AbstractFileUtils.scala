package com.anjunar.jcommander.files

import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.Ref
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasText.text
import com.anjunar.javafx.dsl.traits.IstTextInput.promptText
import com.anjunar.javafx.scene.control.{button, label, textField}
import com.anjunar.javafx.scene.layout.hbox
import com.anjunar.javafx.scene.window.{close, closeWithResult}
import com.anjunar.javafx.scene.{header, window}
import com.anjunar.javafx.stage.Window
import com.anjunar.jcommander.components.DarkModeComponent
import com.anjunar.jcommander.configuration.DarkModeConf
import com.anjunar.jcommander.dsl.RenameFileWindow.directoryName
import com.anjunar.jcommander.dsl.{FileTable, MakeDirectoryWindow, RenameFileWindow}
import com.anjunar.jcommander.ui.ThemedDialog
import com.anjunar.jcommander.utils.CdiUtils.*
import com.typesafe.scalalogging.Logger

import java.io.File
import java.nio.file.{Files, Path}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class AbstractFileUtils extends FileUtils {
  
  val log = Logger[AbstractFileUtils]
  
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

  def mkDir(activeTable: FileTable): Unit = {
    
    val dialog = component[Window[String]] {
      MakeDirectoryWindow() { }
    }
    
    dialog.showAndWaitResult().foreach { result =>
      val newFileName = result
      Files.createDirectory(new File(activeTable.directoryProperty.get()).toPath.resolve(newFileName))
    }
  }

  def renameFile(activeTable: FileTable): Unit = {
    
    val dialog = component[Window[String]] {
      RenameFileWindow() {
        directoryName = activeTable.node.getSelectionModel.getSelectedItem.name
      }
    }
    
    dialog.showAndWaitResult().foreach { result =>
      val newFileName = result
      val oldPath = activeTable.node.getSelectionModel.getSelectedItems.get(0)
      val newPath = Path.of(oldPath.parent).resolve(newFileName)
      Files.move(Path.of(oldPath.file), newPath)
    }
  }


}

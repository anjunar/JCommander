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
import com.anjunar.jcommander.configuration.DarkModeConf
import com.anjunar.jcommander.dsl.dialog.RenameFileDialog.directoryName
import com.anjunar.jcommander.dsl.dialog.{MakeDirectoryDialog, RenameFileDialog}
import com.anjunar.jcommander.dsl.FileTable
import com.anjunar.jcommander.utils.CdiUtils.*
import com.typesafe.scalalogging.Logger

import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.{Files, Path}
import javax.swing.Icon
import javax.swing.filechooser.FileSystemView
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class AbstractFileUtils extends FileUtils {
  
  val log = Logger[AbstractFileUtils]

  private def iconToBufferedImage(icon: Icon): BufferedImage = {
    val w = icon.getIconWidth
    val h = icon.getIconHeight
    val image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val g = image.createGraphics()
    icon.paintIcon(null, g, 0, 0)
    g.dispose()
    image
  }

  override def getFileIcon(file: String, large: Boolean): BufferedImage = {
    val f = new File(file)
    val icon = FileSystemView.getFileSystemView.getSystemIcon(f)
    iconToBufferedImage(icon)
  }
  
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
      MakeDirectoryDialog() { }
    }

    dialog.showAndWaitResult().foreach { result =>
      val newFileName = result
      Files.createDirectory(new File(activeTable.directoryProperty.get()).toPath.resolve(newFileName))
    }
  }

  def renameFile(activeTable: FileTable): Unit = {

    val dialog = component[Window[String]] {
      RenameFileDialog() {
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

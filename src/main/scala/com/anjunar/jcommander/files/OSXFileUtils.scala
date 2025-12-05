package com.anjunar.jcommander.files

import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasText.{text, textProperty}
import com.anjunar.javafx.scene.control.{button, checkbox, label}
import com.anjunar.javafx.scene.control.checkbox.selectedProperty
import com.anjunar.javafx.scene.layout.hbox
import com.anjunar.javafx.scene.window
import com.anjunar.javafx.scene.window.close
import com.anjunar.javafx.stage.Window
import com.anjunar.jcommander.commands.{DeleteCommand, RenameCommand}
import com.anjunar.jcommander.dsl.{ConfirmDialog, FileTable, ProgressDialog}
import com.anjunar.jcommander.ui.ThemedDialog
import com.anjunar.jcommander.utils.CdiUtils.inject
import com.anjunar.jcommander.{Icons, OSXNativeCopy}
import javafx.application.Platform
import javafx.beans.property.{SimpleBooleanProperty, SimpleStringProperty}
import javafx.concurrent
import javafx.scene.input.MouseEvent

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File}
import java.nio.file.attribute.{PosixFileAttributes, PosixFilePermissions}
import java.nio.file.{Files, Path, Paths}
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import javax.imageio.ImageIO
import scala.jdk.CollectionConverters.*
import scala.sys.process.*

class OSXFileUtils extends AbstractFileUtils, UnixFileUtils {

  private var contextMenuOpen = false

  override def console(workingDir: String): Unit =
    new Thread(() => { Seq("open", "-a", "Terminal", workingDir).!; () }).start()

  override def executeFile(file: String): Unit =
    new Thread(() => {
      val out = new StringBuilder
      val log = ProcessLogger(line => out.append(line).append("\n"))
      val exit = Seq("open", file).!(log)
      if (exit != 0) {
        val msg = out.toString.trim match {
          case s if s.nonEmpty => s
          case _ => s"File could not be opened: $file"
        }
        Platform.runLater { () =>
          val dialog = component[Window[Unit]] {
            window[Unit]() {
              label() {
                text = "Open Error"
              }
              label() {
                text = "The file could not be opened"
              }
              label() {
                text = msg
              }
              
              hbox() {
                button() {
                  text = "Ok"
                  onAction = _ => {
                    close()
                  }
                }
              }
              
            }
          }
          dialog.showAndWait()
        }
      }
    }).start()


  override def getFileIcon(file: String, large: Boolean): BufferedImage = {
    val bytes = OSXNativeCopy.getFileIcon(file, large)
    ImageIO.read(new ByteArrayInputStream(bytes))
  }

  override def deleteFiles(activeTable: FileTable, otherTable: FileTable): Unit =
    processFiles(
      (paths: Seq[Path], target: Path, overwrite, recycle, ProgressListener: OSXNativeCopy.ProgressListener) => {
        OSXNativeCopy.deleteFiles(paths.map(_.toAbsolutePath.toString).toArray, recycle, ProgressListener)
      },
      "Delete Files",
      "Should the selected Files be deleted?",
      "Deleting Files...",
      true,
      activeTable,
      otherTable
    )

  override def copyFiles(activeTable: FileTable, otherTable: FileTable): Unit =
    processFiles(
      (paths: Seq[Path], target: Path, overwrite, recycle, ProgressListener: OSXNativeCopy.ProgressListener) => {
        OSXNativeCopy.copyFiles(paths.map(_.toAbsolutePath.toString).toArray, target.toAbsolutePath.toString, overwrite, ProgressListener)
      },
      "Copy Files",
      "Should the selected Files be copied?",
      "Copying Files...",
      false,
      activeTable,
      otherTable
    )

  override def moveFiles(activeTable: FileTable, otherTable: FileTable): Unit =
    processFiles(
      (paths: Seq[Path], target: Path, overwrite, recycle, ProgressListener: OSXNativeCopy.ProgressListener) => {
        OSXNativeCopy.moveFiles(paths.map(_.toAbsolutePath.toString).toArray, target.toAbsolutePath.toString, overwrite, ProgressListener)
      },
      "Move Files",
      "Should the selected Files be moved?",
      "Moving Files...",
      false,
      activeTable,
      otherTable
    )

  private def formatEta(seconds: Int): String =
    if (seconds <= 0) "Calculating..."
    else if (seconds < 60) s"$seconds sec remaining"
    else {
      val minutes = seconds / 60
      val sec = seconds % 60
      if (minutes < 60) {
        if (sec == 0) s"$minutes min remaining"
        else s"$minutes min $sec sec remaining"
      } else {
        val hours = minutes / 60
        val min = minutes % 60
        if (min == 0) s"$hours h remaining"
        else s"$hours h $min min remaining"
      }
    }

  def processFiles(
                    strategy: OSXFileStrategy,
                    confirmTitle: String,
                    confirmHeader: String,
                    progressText: String,
                    isDelete: Boolean,
                    activeTable: FileTable,
                    otherTable: FileTable
                  ): Unit = {

    val replaceExistingBox = new SimpleBooleanProperty(false)
    val moveToRecycleBinBox = new SimpleBooleanProperty(true)

    val confirmDialog = component[Window[String]] {
      ConfirmDialog(isDelete) {

        label.unwrap("header") {
          text = confirmTitle
        }

        label.unwrap("confirm") {
          text = confirmHeader
        }

        checkbox.unwrap("moveToRecycle") {
          selectedProperty(prop => moveToRecycleBinBox.bindBidirectional(prop))
        }

        checkbox.unwrap("replaceExisting") {
          selectedProperty(prop => replaceExistingBox.bindBidirectional(prop))
        }
      }
    }

    confirmDialog.showAndWaitResult().foreach { result =>
      if (result == "Ok") {

        val overwriteExisting = replaceExistingBox.get()
        val moveToRecycleBin = moveToRecycleBinBox.get()

        val selectedFiles = activeTable.node.getSelectionModel.getSelectedItems.asScala.map(item => Path.of(item.file)).toSeq
        val targetDir = if isDelete then Path.of(activeTable.directoryProperty.get()) else Path.of(otherTable.directoryProperty.get())

        val cancelledFlag = new AtomicBoolean(false)

        val progressString = new SimpleStringProperty()
        val fileString = new SimpleStringProperty()

        val task = new concurrent.Task[Unit]() {
          override def call(): Unit = {
            val startTime = Instant.now()

            strategy.winProcess(
              selectedFiles,
              targetDir,
              overwriteExisting,
              moveToRecycleBin,
              new OSXNativeCopy.ProgressListener {

                override def onFileProgress(operation: Int, source: String, target: String, bytesDone: Long, bytesTotal: Long): Unit = {
                  val percent = if bytesTotal == 0 then 0 else bytesDone.toDouble / bytesTotal
                  updateProgress(bytesDone, bytesTotal)
                  Platform.runLater { () =>
                    progressString.set(f"${percent * 100}%.0f%%")
                    fileString.set(source)
                  }
                }

                override def onFileComplete(operation: Int, source: String, target: String): Unit = {}

                override def onComplete(operation: Int): Unit =
                  Platform.runLater { () => progressString.set("Done") }

                override def onError(operation: Int, source: String, target: String, code: Int, message: String): Unit = {
                  operation match {
                    case 0 =>
                      Seq("osascript", "-e", s"""do shell script "cp -R '$source' '$target'" with administrator privileges""").!
                    case 1 =>
                      Seq("osascript", "-e", s"""do shell script "mv '$source' '$target'" with administrator privileges""").!
                    case 2 =>
                      if (moveToRecycleBin)
                        Seq("osascript", "-e", s"""tell application "Finder" to delete POSIX file "$source"""").!
                      else
                        Seq("osascript", "-e", s"""do shell script "rm -R '$source'" with administrator privileges""").!
                  }
                }

                override def isCancelled: Boolean = cancelledFlag.get()
              }
            )
          }
        }

        val progressDialog = component[Window[Unit]] {
          ProgressDialog(cancelledFlag, task) {

            label.unwrap("progressText") {
              text = progressText
            }

            label.unwrap("progress") {
              textProperty(prop => progressString.bindBidirectional(prop))
            }

            label.unwrap("file") {
              textProperty(prop => fileString.bindBidirectional(prop))
            }
          }
        }

        task.setOnSucceeded { _ =>
          progressDialog.close()
        }

        task.setOnFailed { _ =>
          progressDialog.close()
          log.error("Task failed", task.getException)
        }

        task.setOnCancelled { _ =>
          progressDialog.close()
          log.info("Task was cancelledFlag.")
        }

        Platform.runLater { () =>
          progressDialog.show()
        }
        new Thread(task).start()
      }
    }
  }

}

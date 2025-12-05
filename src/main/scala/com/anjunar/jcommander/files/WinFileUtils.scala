package com.anjunar.jcommander.files

import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasPadding.padding
import com.anjunar.javafx.dsl.traits.HasSpacing.{alignment, spacing}
import com.anjunar.javafx.dsl.traits.HasText.{text, textProperty}
import com.anjunar.javafx.dsl.traits.HasWidth.prefWidth
import com.anjunar.javafx.dsl.traits.IsNode.vgrow
import com.anjunar.javafx.scene.window.{close, closeWithResult}
import com.anjunar.javafx.scene.control.checkbox.selectedProperty
import com.anjunar.javafx.scene.control.progressBar.progressProperty
import com.anjunar.javafx.scene.control.{button, checkbox, label, progressBar}
import com.anjunar.javafx.scene.layout.{hbox, region, vbox}
import com.anjunar.javafx.scene.{header, window}
import com.anjunar.javafx.stage.Window
import com.anjunar.jcommander.WinNativeCopy
import com.anjunar.jcommander.dsl.ConfirmDialog.{confirmHeader, confirmText, moveToRecycle, replaceExisting}
import com.anjunar.jcommander.dsl.{ConfirmDialog, FileTable, ProgressDialog}
import com.typesafe.scalalogging.Logger
import javafx.application.Platform
import javafx.beans.property.{SimpleBooleanProperty, SimpleStringProperty}
import javafx.concurrent
import javafx.geometry.{Insets, Pos}
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File}
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import javax.imageio.ImageIO
import scala.jdk.CollectionConverters.*

class WinFileUtils extends AbstractFileUtils {

  override val log = Logger[WinFileUtils]

  override def fileContext(files: Seq[String], event: MouseEvent): Unit = {
    WinNativeCopy.fileContext(files.toArray, true)
  }

  override def console(workingDir: String): Unit = {
    new ProcessBuilder(
      "cmd.exe", "/c", "start", "powershell",
      "-NoExit", "-Command",
      s"Set-Location '${workingDir}'"
    ).directory(new File(workingDir)).start()
  }

  override def getFileIcon(file: String, large: Boolean): BufferedImage = {
    val bytes = WinNativeCopy.getFileIcon(file, large)
    ImageIO.read(new ByteArrayInputStream(bytes))
  }

  override def executeFile(file: String): Unit = WinNativeCopy.executeFile(file)

  override def copyFiles(activeTable: FileTable, otherTable: FileTable): Unit = {
    processFiles(
      (paths: Seq[Path], target: Path, overwrite, recycle, progressCallback: WinNativeCopy.ProgressCallback) => {
        WinNativeCopy.copyFiles(paths.map(_.toAbsolutePath.toString).toArray, target.toAbsolutePath.toString, progressCallback, overwrite)
      },
      "Copy Files",
      "Should the selected Files be copied?",
      "Copying Files...",
      false,
      activeTable,
      otherTable
    )
  }

  override def moveFiles(activeTable: FileTable, otherTable: FileTable): Unit = {
    processFiles(
      (paths: Seq[Path], target: Path, overwrite, recycle, progressCallback: WinNativeCopy.ProgressCallback) => {
        WinNativeCopy.moveFiles(paths.map(_.toAbsolutePath.toString).toArray, target.toAbsolutePath.toString, progressCallback, overwrite)
      },
      "Move Files",
      "Should the selected Files be moved?",
      "Moving Files...",
      false,
      activeTable,
      otherTable
    )
  }

  override def deleteFiles(activeTable: FileTable, otherTable: FileTable): Unit = {
    processFiles(
      (paths: Seq[Path], target: Path, overwrite, recycle, progressCallback: WinNativeCopy.ProgressCallback) => {
        WinNativeCopy.deleteFiles(paths.map(_.toAbsolutePath.toString).toArray, progressCallback, recycle)
      },
      "Delete Files",
      "Should the selected Files be deleted?",
      "Deleting Files...",
      true,
      activeTable,
      otherTable
    )
  }

  private def formatEta(seconds: Int): String = {
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
  }

  def processFiles(
                    strategy: WinFileStrategy,
                    confirmTitleText: String,
                    confirmHeaderText: String,
                    progressTextText: String,
                    isDelete: Boolean,
                    activeTable: FileTable,
                    otherTable: FileTable
                  ): Unit = {

    val replaceExistingBox = new SimpleBooleanProperty(false)
    val moveToRecycleBinBox = new SimpleBooleanProperty(true)

    val confirmDialog = component[Window[String]] {
      ConfirmDialog(isDelete) {
        
        label.unwrap("header") {
          text = confirmTitleText
        }
        
        label.unwrap("confirm") {
          text = confirmHeaderText
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

            strategy.winProcess(selectedFiles, targetDir, overwriteExisting, moveToRecycleBin, new WinNativeCopy.ProgressCallback {

              override def onProgress(event: WinNativeCopy.ProgressEvent): Unit = {
                event.`type` match {
                  case WinNativeCopy.ProgressEvent.Type.UPDATE =>
                    updateProgress(event.percent, 1.0)

                    if (isDelete) {
                      Platform.runLater { () =>
                        fileString.set(event.source)
                      }
                    }

                    val elapsed = java.time.Duration.between(startTime, Instant.now()).toMillis
                    val eta = if (event.percent > 0) (elapsed / event.percent) - elapsed else 0
                    val etaSec = (eta / 1000).toInt

                    Platform.runLater { () => 
                      val text = f"${(event.percent * 100).toInt}%% copied  –  (${formatEta(etaSec)})"
                      progressString.set(text)
                    }

                  case WinNativeCopy.ProgressEvent.Type.PRE_COPY =>
                    Platform.runLater { () => 
                      fileString.set(event.source)
                    }

                  case WinNativeCopy.ProgressEvent.Type.FINISH =>
                    Platform.runLater { () => 
                      progressString.set("Finishing...")
                    }

                  case _ => // ignore
                }
              }

              override def onError(event: WinNativeCopy.ErrorEvent): Unit = {
                log.error(s"File error: ${event.toString}")
              }

              override def onComplete(): Unit = {
                log.info("Operation completed successfully.")
                Platform.runLater { () => 
                  progressString.set("Completed!")
                }
              }

              override def isCancelled: Boolean = cancelledFlag.get()
            })
          }
        }

        val progressDialog = component[Window[Unit]] {
          ProgressDialog(cancelledFlag, task) {
            
            label.unwrap("progressText") {
              text = progressTextText
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
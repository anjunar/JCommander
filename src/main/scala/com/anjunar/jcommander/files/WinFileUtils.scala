package com.anjunar.jcommander.files

import com.anjunar.jcommander.components.FileTableComponent
import com.anjunar.jcommander.ui.ThemedDialog
import com.anjunar.jcommander.{OSType, WinNativeCopy}
import com.typesafe.scalalogging.Logger
import javafx.concurrent
import scalafx.Includes.jfxDialogPane2sfx
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.scene.control.*
import scalafx.scene.layout.{Pane, VBox}

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File}
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import javax.imageio.ImageIO
import scala.jdk.CollectionConverters.*

class WinFileUtils extends AbstractFileUtils {

  override val log = Logger[WinFileUtils]

  override def fileContext(file: File): Unit = {
    WinNativeCopy.fileContext(file.getAbsolutePath, true)
  }

  override def console(workingDir: File): Unit = {
    new ProcessBuilder(
      "cmd.exe", "/c", "start", "powershell",
      "-NoExit", "-Command",
      s"Set-Location '${workingDir.getAbsolutePath}'"
    ).directory(workingDir).start()
  }

  override def getFileIcon(file: File, large: Boolean): BufferedImage = {
    val bytes = WinNativeCopy.getFileIcon(file.getAbsolutePath, large)
    ImageIO.read(new ByteArrayInputStream(bytes))
  }

  override def executeFile(file: File): Unit = WinNativeCopy.executeFile(file.getAbsolutePath)

  override def copyFiles(activeTable: FileTableComponent, otherTable: FileTableComponent): Unit = {
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

  override def moveFiles(activeTable: FileTableComponent, otherTable: FileTableComponent): Unit = {
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

  override def deleteFiles(activeTable: FileTableComponent, otherTable: FileTableComponent): Unit = {
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

  def processFiles(
                    strategy: WinFileStrategy,
                    confirmTitle: String,
                    confirmHeader: String,
                    progressText: String,
                    isDelete: Boolean,
                    activeTable: FileTableComponent,
                    otherTable: FileTableComponent
                  ): Unit = {

    val replaceExistingBox = new CheckBox("Replace existing files") {
      selected = false
    }
    val moveToRecycleBinBox = new CheckBox("Move to Recycle Bin") {
      selected = true
    }

    val confirmDialog = new ThemedDialog[ButtonType] {
      title = confirmTitle
      headerText = confirmHeader
      dialogPane.buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
      dialogPane.content = new VBox(10) {
        if (!isDelete) {
          children += replaceExistingBox
        } else {
          children += moveToRecycleBinBox
        }
      }
    }

    confirmDialog.resultConverter = identity

    confirmDialog.showAndWaitDialog().foreach { result =>
      if (result == ButtonType.OK) {

        val overwriteExisting = replaceExistingBox.selected.value
        val moveToRecycleBin = moveToRecycleBinBox.selected.value

        val selectedFiles = activeTable.node.selectionModel.value.getSelectedItems.asScala.map(_.file.toPath).toSeq
        val targetDir = otherTable.directory.toPath

        val cancelledFlag = new AtomicBoolean(false)

        val progressBar = new ProgressBar {
          prefWidth = 350
        }
        val progressLabel = new Label("0% copied")
        val fileLabel = new Label("")

        val task = new concurrent.Task[Unit]() {
          override def call(): Unit = {
            val startTime = Instant.now()

            strategy.winProcess(selectedFiles, targetDir, overwriteExisting, moveToRecycleBin, new WinNativeCopy.ProgressCallback {

              override def onProgress(event: WinNativeCopy.ProgressEvent): Unit = {
                event.`type` match {
                  case WinNativeCopy.ProgressEvent.Type.UPDATE =>
                    updateProgress(event.percent, 1.0)

                    val elapsed = java.time.Duration.between(startTime, Instant.now()).toMillis
                    val eta = if (event.percent > 0) (elapsed / event.percent) - elapsed else 0
                    val etaSec = (eta / 1000).toInt

                    val etaText =
                      if (etaSec > 0) f"$etaSec sec remaining"
                      else "Calculating..."

                    Platform.runLater {
                      progressLabel.text =
                        f"${(event.percent * 100).toInt}%% copied  –  ($etaText)"
                    }

                  case WinNativeCopy.ProgressEvent.Type.PRE_COPY =>
                    Platform.runLater {
                      fileLabel.text = event.source
                    }

                  case WinNativeCopy.ProgressEvent.Type.FINISH =>
                    Platform.runLater {
                      progressLabel.text = "Finishing..."
                    }

                  case _ => // ignore
                }
              }

              override def onError(event: WinNativeCopy.ErrorEvent): Unit = {
                log.error(s"File error: ${event.toString}")
              }

              override def onComplete(): Unit = {
                log.info("Operation completed successfully.")
                Platform.runLater {
                  progressLabel.text = "Completed!"
                }
              }

              override def isCancelled: Boolean = cancelledFlag.get()
            })
          }
        }

        val progressDialog = new ThemedDialog[Unit]() {
          title = progressText
          dialogPane.content = new VBox(10, progressBar, progressLabel, fileLabel)
          dialogPane.buttonTypes = Seq(ButtonType.Cancel)
        }

        val cancelButton = progressDialog.dialogPane.lookupButton(ButtonType.Cancel).asInstanceOf[javafx.scene.control.Button]
        cancelButton.addEventFilter(ActionEvent.Action, _ => {
          cancelledFlag.set(true) 
          task.cancel()
          progressDialog.close()
          log.info("Operation cancelledFlag by user.")
        })

        progressBar.progress <== task.progressProperty()

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

        // Starte Task in eigenem Thread
        Platform.runLater {
          progressDialog.show()
        }
        new Thread(task).start()
      }
    }
  }
}
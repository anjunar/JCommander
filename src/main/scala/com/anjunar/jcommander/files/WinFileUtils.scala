package com.anjunar.jcommander.files

import com.anjunar.jcommander.{FileTable, Main, WinNativeCopy}
import com.typesafe.scalalogging.Logger
import javafx.concurrent
import scalafx.Includes.jfxDialogPane2sfx
import scalafx.application.Platform
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.event.ActionEvent
import scalafx.scene.control.*
import scalafx.scene.layout.VBox

import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

class WinFileUtils extends AbstractFileUtils {

  val log = Logger[WinFileUtils]

  override def copyFiles(activeTable: ObjectProperty[FileTable], otherTable: ObjectProperty[FileTable], darkMode: BooleanProperty): Unit = {
    processFiles(
      (paths: Seq[Path], target: Path, progressCallback: WinNativeCopy.ProgressCallback) => {
        WinNativeCopy.copyFiles(paths.map(_.toAbsolutePath.toString).toArray, target.toAbsolutePath.toString, progressCallback)
      },
      "Copy Files",
      "Should the selected Files be copied?",
      "Copying Files...",
      false,
      activeTable,
      otherTable,
      darkMode
    )
  }

  override def moveFiles(activeTable: ObjectProperty[FileTable], otherTable: ObjectProperty[FileTable], darkMode: BooleanProperty): Unit = {
    processFiles(
      (paths: Seq[Path], target: Path, progressCallback: WinNativeCopy.ProgressCallback) => {
        WinNativeCopy.moveFiles(paths.map(_.toAbsolutePath.toString).toArray, target.toAbsolutePath.toString, progressCallback)
      },
      "Move Files",
      "Should the selected Files be moved?",
      "Moving Files...",
      false,
      activeTable,
      otherTable,
      darkMode
    )
  }

  override def deleteFiles(activeTable: ObjectProperty[FileTable], otherTable: ObjectProperty[FileTable], darkMode: BooleanProperty): Unit = {
    processFiles(
      (paths: Seq[Path], target: Path, progressCallback: WinNativeCopy.ProgressCallback) => {
        WinNativeCopy.deleteFiles(paths.map(_.toAbsolutePath.toString).toArray, progressCallback)
      },
      "Delete Files",
      "Should the selected Files be deleted?",
      "Deleting Files...",
      true,
      activeTable,
      otherTable,
      darkMode
    )
  }

  def processFiles(
                    strategy: WinFileStrategy,
                    confirmTitle: String,
                    confirmHeader: String,
                    progressText: String,
                    isDelete: Boolean,
                    activeTable: ObjectProperty[FileTable],
                    otherTable: ObjectProperty[FileTable],
                    darkMode: BooleanProperty
                  ): Unit = {

    val replaceExistingBox = new CheckBox("Replace existing files") { selected = true }
    val copyAttributesBox = new CheckBox("Copying Attributes") { selected = false }
    val checkLockedFilesBox = new CheckBox("Check for locked Files") { selected = false }

    val confirmDialog = new Dialog[ButtonType]() {
      title = confirmTitle
      headerText = confirmHeader
      dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
      dialogPane().content = new VBox(10) {
        if (!isDelete) {
          if (Main.osName == "win") {
            children += replaceExistingBox
          } else {
            children ++= Seq(replaceExistingBox, copyAttributesBox)
          }
        } else {
          children += checkLockedFilesBox
        }
      }
      dialogPane().getStylesheets.add(
        getClass.getResource(s"/${if (darkMode.value) "dark" else "light"}-theme.css").toExternalForm
      )
    }

    confirmDialog.resultConverter = identity

    confirmDialog.showAndWait().foreach { result =>
      if (result == ButtonType.OK) {

        val selectedFiles = activeTable.value.selectionModel.value.getSelectedItems.asScala.map(_.file.toPath).toSeq
        val targetDir = otherTable.value.directory.toPath

        val cancelledFlag = new AtomicBoolean(false)

        val progressBar = new ProgressBar { prefWidth = 350 }
        val progressLabel = new Label("0% copied")
        val fileLabel = new Label("")

        val task = new concurrent.Task[Unit]() {
          override def call(): Unit = {
            val startTime = Instant.now()

            strategy.winProcess(selectedFiles, targetDir, new WinNativeCopy.ProgressCallback {

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

        val progressDialog = new Dialog[Unit]() {
          title = progressText
          dialogPane().content = new VBox(10, progressBar, progressLabel, fileLabel)
          dialogPane().buttonTypes = Seq(ButtonType.Cancel)
          dialogPane().getStylesheets.add(
            getClass.getResource(s"/${if (darkMode.value) "dark" else "light"}-theme.css").toExternalForm
          )
        }

        // Cancel-Button: Setzt Flag und schließt Dialog
        val cancelButton = progressDialog.dialogPane().lookupButton(ButtonType.Cancel).asInstanceOf[javafx.scene.control.Button]
        cancelButton.addEventFilter(ActionEvent.ACTION, _ => {
          cancelledFlag.set(true)  // <-- WICHTIG: Setzt Abbruch-Flag
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
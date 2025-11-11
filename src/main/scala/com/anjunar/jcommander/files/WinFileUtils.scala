package com.anjunar.jcommander.files

import com.anjunar.jcommander.{FileTable, WinNativeCopy}
import com.typesafe.scalalogging.Logger
import javafx.concurrent
import scalafx.Includes.jfxDialogPane2sfx
import scalafx.application.Platform
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.event.ActionEvent
import scalafx.scene.control.*
import scalafx.scene.layout.VBox

import java.nio.file.Path
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

class WinFileUtils extends AbstractFileUtils {

  val log = Logger[WinFileUtils]

  override def copyFiles(activeTable: ObjectProperty[FileTable], otherTable: ObjectProperty[FileTable], darkMode: BooleanProperty): Unit = {
    processFiles(
      (paths: Seq[Path], target: Path, progressCallback: WinNativeCopy.ProgressCallback) => {
        WinNativeCopy.copyFiles(paths.map(path => path.toAbsolutePath.toString).toArray, target.toAbsolutePath.toString, progressCallback)
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
        WinNativeCopy.moveFiles(paths.map(path => path.toAbsolutePath.toString).toArray, target.toAbsolutePath.toString, progressCallback)
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
        WinNativeCopy.deleteFiles(paths.map(path => path.toAbsolutePath.toString).toArray, progressCallback)
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

  def processFiles(strategy: WinFileStrategy,
                   confirmTitle: String,
                   confirmHeader: String,
                   progressText: String,
                   isDelete: Boolean,
                   activeTable: ObjectProperty[FileTable],
                   otherTable: ObjectProperty[FileTable],
                   darkMode: BooleanProperty): Unit = {

    val replaceExistingBox = new CheckBox("Replace existing files") {
      selected = true
    }

    val copyAttributesExistingBox = new CheckBox("Copying Attributes") {
      selected = false
    }

    val checkForLockedFilesBox = new CheckBox("Check for locked Files") {
      selected = false
    }

    val confirmDialog = new Dialog[ButtonType]() {
      title = confirmTitle
      headerText = confirmTitle
      dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
      if (!isDelete) {
        if (osName == "win") {
          dialogPane().content = new VBox(10, replaceExistingBox)
        } else {
          dialogPane().content = new VBox(10, replaceExistingBox, copyAttributesExistingBox)
        }
      } else {
        dialogPane().content = new VBox(10, checkForLockedFilesBox)
      }
      dialogPane().getStylesheets.add(
        getClass.getResource(s"/${if darkMode.value then "dark" else "light"}-theme.css").toExternalForm
      )
    }

    confirmDialog.resultConverter = btn => btn

    confirmDialog.showAndWait().foreach { result =>
      if (result == ButtonType.OK) {

        val replaceExisting = replaceExistingBox.selected.value
        val copyAttributes = copyAttributesExistingBox.selected.value
        val checkForLockedFiles = checkForLockedFilesBox.selected.value

        val lockedFiles = new ListBuffer[Path]
        val selectedItems = activeTable.value.selectionModel.value.getSelectedItems

        val selectedFiles = selectedItems.asScala.map(file => file.file.toPath).toSeq

        val progressBar = new ProgressBar {
          prefWidth = 350
        }

        val progressLabel = new Label("0 MB copied (0 MB/s)")

        val task = new concurrent.Task[Unit]() {
          override def call(): Unit = {

            strategy.winProcess(selectedFiles, otherTable.value.directory.toPath, new WinNativeCopy.ProgressCallback {
              override def onProgress(event: WinNativeCopy.ProgressEvent): Unit = {
                if (event.`type` == WinNativeCopy.ProgressEvent.Type.UPDATE) {
                  updateProgress(event.percent, 1)
                }
                log.info(event.toString)
              }

              override def onError(event: WinNativeCopy.ErrorEvent): Unit = log.info(event.toString)

              override def onComplete(): Unit = log.info("Complete")
            })


          }
        }

        val progressDialog = new Dialog[Unit]() {
          title = progressText
          dialogPane().content = new VBox(10, progressBar, progressLabel)
          dialogPane().buttonTypes = Seq(ButtonType.Cancel)
          dialogPane().getStylesheets.add(
            getClass.getResource(s"/${if darkMode.value then "dark" else "light"}-theme.css").toExternalForm
          )
        }

        progressDialog.dialogPane().lookupButton(ButtonType.Cancel).addEventFilter(ActionEvent.Action, _ => {
          task.cancel()
          progressDialog.close()
        })

        progressBar.progress <== task.progressProperty()

        task.setOnSucceeded { _ =>
          progressDialog.close()
        }

        task.setOnFailed { _ =>
          progressDialog.close()
        }

        Platform.runLater {
          progressDialog.show()
        }
        new Thread(task).start()

      }

    }
  }
}

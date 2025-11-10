package com.anjunar.jcommander

import javafx.concurrent
import scalafx.Includes.jfxDialogPane2sfx
import scalafx.application.Platform
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.event.ActionEvent
import scalafx.scene.control.{ButtonType, CheckBox, Dialog, ProgressBar}
import scalafx.scene.layout.VBox

import java.nio.file.{Files, Path, StandardCopyOption}
import scala.jdk.CollectionConverters.*

object FileUtils {

  def copyFiles(activeTable: ObjectProperty[FileTable],
                otherTable: ObjectProperty[FileTable],
                darkMode: BooleanProperty): Unit = {
    processFiles(
      (path: Path, target: Path, replaceExisting : Boolean) => {
        if (replaceExisting) {
          Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING)
        } else {
          Files.copy(path, target)
        }
      },
      "Copy Files",
      "Should the selected Files be copied?",
      "Coping Files...",
      activeTable,
      otherTable,
      darkMode
    )
  }

  def moveFiles(activeTable: ObjectProperty[FileTable],
                otherTable: ObjectProperty[FileTable],
                darkMode: BooleanProperty): Unit = {
    processFiles(
      (path: Path, target: Path, replaceExisting : Boolean) => {
        val sameDrive =
          path.getRoot != null &&
            target.getRoot != null &&
            path.getRoot.equals(target.getRoot)

        if sameDrive then {
          if (replaceExisting) {
            Files.move(path, target, StandardCopyOption.REPLACE_EXISTING)  
          } else {
            Files.move(path, target)
          }
        } else
          if (replaceExisting) {
            Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING)
          } else {
            Files.copy(path, target)
          }
          Files.delete(path)
      },
      "Move Files",
      "Should the selected Files be moved?",
      "Moving Files...",
      activeTable,
      otherTable,
      darkMode
    )
  }

  def deleteFiles(activeTable: ObjectProperty[FileTable],
                otherTable: ObjectProperty[FileTable],
                darkMode: BooleanProperty): Unit = {
    processFiles(
      (path: Path, target: Path, replaceExisting : Boolean) => Files.delete(target),
      "Delete Files",
      "Should the selected Files be deleted?",
      "Deleting Files...",
      activeTable,
      otherTable,
      darkMode
    )
  }

  def processFiles(strategy: FileStrategy,
                   confirmTitle : String,
                   confirmHeader : String,
                   progressText : String,
                   activeTable: ObjectProperty[FileTable],
                   otherTable: ObjectProperty[FileTable],
                   darkMode: BooleanProperty): Unit = {

    val replaceExistingBox = new CheckBox("Replace existing files") {
      selected = true
    }

    val confirmDialog = new Dialog[ButtonType]() {
      title = confirmTitle
      headerText = confirmTitle
      dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
      dialogPane().content = new VBox(10, replaceExistingBox)
      dialogPane().getStylesheets.add(
        getClass.getResource(s"/${if darkMode.value then "dark" else "light"}-theme.css").toExternalForm
      )
    }

    confirmDialog.resultConverter = btn => btn

    confirmDialog.showAndWait().foreach { result =>
      if (result == ButtonType.OK) {

        val replaceExisting = replaceExistingBox.selected.value

        val selectedItems = activeTable.value.selectionModel.value.getSelectedItems
        val allFiles = selectedItems.stream().flatMap { fileItem =>
          val path = fileItem.file.toPath
          if (Files.isDirectory(path)) Files.walk(path)
          else java.util.stream.Stream.of(path)
        }.toList.asScala.toSeq

        val progressBar = new ProgressBar {
          prefWidth = 350
        }

        val task = new concurrent.Task[Unit]() {
          override def call(): Unit = {
            val total = allFiles.size
            allFiles.zipWithIndex.foreach { case (path, i) =>
              if (isCancelled) return
              val target = otherTable.value.directory.toPath.resolve(path.getFileName)

              strategy.process(path, target, replaceExisting)

              updateProgress(i + 1, total)
            }
          }
        }

        val progressDialog = new Dialog[Unit]() {
          title = progressText
          dialogPane().content = new VBox(10, progressBar)
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

        task.setOnFailed { e =>
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

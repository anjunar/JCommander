package com.anjunar.jcommander

import com.typesafe.scalalogging.Logger
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

  val log = Logger[FileUtils.type]

  def copyFiles(activeTable: ObjectProperty[FileTable],
                otherTable: ObjectProperty[FileTable],
                darkMode: BooleanProperty): Unit = {
    processFiles(
      (path: Path, target: Path, replaceExisting : Boolean, copyAttributes : Boolean) => {

        val copyOption = copyOptions(replaceExisting, copyAttributes)

        Files.copy(path, target, copyOption*)
      },
      "Copy Files",
      "Should the selected Files be copied?",
      "Coping Files...",
      false,
      activeTable,
      otherTable,
      darkMode
    )
  }

  def moveFiles(activeTable: ObjectProperty[FileTable],
                otherTable: ObjectProperty[FileTable],
                darkMode: BooleanProperty): Unit = {
    processFiles(
      (path: Path, target: Path, replaceExisting : Boolean, copyAttributes : Boolean) => {
        val sameDrive =
          path.getRoot != null &&
            target.getRoot != null &&
            path.getRoot.equals(target.getRoot)

        val copyOption = copyOptions(replaceExisting, copyAttributes)

        if sameDrive then {
          Files.move(path, target, copyOption*)
        } else
          Files.copy(path, target, copyOption*)
          setWriteable(path)
          Files.delete(path)
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

  private def copyOptions(replaceExisting: Boolean, copyAttributes: Boolean) = {
    var copyOption = Seq[StandardCopyOption]()
    if (copyAttributes) {
      copyOption = copyOption ++ Seq(StandardCopyOption.COPY_ATTRIBUTES)
    }
    if (replaceExisting) {
      copyOption = copyOption ++ Seq(StandardCopyOption.REPLACE_EXISTING)
    }
    copyOption
  }

  def deleteFiles(activeTable: ObjectProperty[FileTable],
                  otherTable: ObjectProperty[FileTable],
                  darkMode: BooleanProperty): Unit = {
    processFiles(
      (path: Path, target: Path, replaceExisting : Boolean, copyAttributes : Boolean) => {
        setWriteable(path)
        Files.delete(path)
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

  private def setWriteable(path: Path) = {
    if (!path.toFile.canWrite) {
      try {
        path.toFile.setWritable(true)
      } catch {
        case ex: Exception => log.error(ex.getMessage, ex)
      }
    }
  }

  def processFiles(strategy: FileStrategy,
                   confirmTitle : String,
                   confirmHeader : String,
                   progressText : String,
                   isDelete : Boolean,
                   activeTable: ObjectProperty[FileTable],
                   otherTable: ObjectProperty[FileTable],
                   darkMode: BooleanProperty): Unit = {

    val replaceExistingBox = new CheckBox("Replace existing files") {
      selected = true
    }

    val copyAttributesExistingBox = new CheckBox("Copying Attributes") {
      selected = true
    }

    val confirmDialog = new Dialog[ButtonType]() {
      title = confirmTitle
      headerText = confirmTitle
      dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
      if (! isDelete) {
        dialogPane().content = new VBox(10, replaceExistingBox, copyAttributesExistingBox)
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

        val selectedItems = activeTable.value.selectionModel.value.getSelectedItems
        val allFiles = selectedItems.stream().flatMap { fileItem =>
          val path = fileItem.file.toPath
          if (Files.isDirectory(path))
            Files.walk(path).sorted(java.util.Comparator.reverseOrder())
          else
            java.util.stream.Stream.of(path)
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

              try {
                strategy.process(path, target, replaceExisting, copyAttributes)
              } catch {
                case ex : Exception => log.error(ex.getMessage, ex)
              }

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

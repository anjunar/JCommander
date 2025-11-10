package com.anjunar.jcommander

import com.typesafe.scalalogging.Logger
import javafx.concurrent
import scalafx.Includes.{jfxDialogPane2sfx, observableList2ObservableBuffer}
import scalafx.application.Platform
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.event.ActionEvent
import scalafx.scene.control.{ButtonType, CheckBox, Dialog, ProgressBar}
import scalafx.scene.layout.VBox

import java.io.{BufferedInputStream, BufferedOutputStream}
import java.nio.file.{Files, Path, StandardCopyOption, StandardOpenOption}
import scala.jdk.CollectionConverters.*
import scala.util.Using

object FileUtils {

  val log = Logger[FileUtils.type]

  lazy val osName = System.getProperty("os.name") match {
    case n if n.startsWith("Linux") => "linux"
    case n if n.startsWith("Mac") => "mac"
    case n if n.startsWith("Windows") => "win"
    case _ => throw new Exception("Unknown platform!")
  }

  def copyFiles(activeTable: ObjectProperty[FileTable],
                otherTable: ObjectProperty[FileTable],
                darkMode: BooleanProperty): Unit = {
    processFiles(
      (path: Path, target: Path, replaceExisting : Boolean, copyAttributes : Boolean, progressCallback: Double => Unit) => {

        val copyOption = copyOptions(replaceExisting, copyAttributes)

        copyFileWithProgress(path, target, progressCallback)
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

  def moveFiles(activeTable: ObjectProperty[FileTable],
                otherTable: ObjectProperty[FileTable],
                darkMode: BooleanProperty): Unit = {
    processFiles(
      (path: Path, target: Path, replaceExisting : Boolean, copyAttributes : Boolean, progressCallback: Double => Unit) => {
        val sameDrive =
          path.getRoot != null &&
            target.getRoot != null &&
            path.getRoot.equals(target.getRoot)

        val copyOption = copyOptions(replaceExisting, copyAttributes)

        if sameDrive then {
          Files.createDirectories(target.getParent)
          Files.move(path, target, copyOption*)
        } else
          copyFileWithProgress(path, target, progressCallback)
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
      (path: Path, target: Path, replaceExisting : Boolean, copyAttributes : Boolean, progressCallback: Double => Unit) => {
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

        val selectedItems = activeTable.value.selectionModel.value.getSelectedItems
        val allFiles = selectedItems.stream().flatMap { fileItem =>
          val path = fileItem.file.toPath
          if (Files.isDirectory(path))
            Files.walk(path).sorted(java.util.Comparator.reverseOrder())
          else
            java.util.stream.Stream.of(path)
        }.toList.asScala.toSeq

        val lockedFiles = allFiles.filter(file => isFileLocked(file))

        if (lockedFiles.isEmpty || !checkForLockedFiles) {
          val progressBar = new ProgressBar {
            prefWidth = 350
          }

          val task = new concurrent.Task[Unit]() {
            override def call(): Unit = {
              val total = allFiles.size
              val baseSource = selectedItems.head.file.toPath.getParent
              val targetRoot = otherTable.value.directory.toPath

              allFiles.zipWithIndex.foreach { case (path, i) =>
                if (isCancelled) return

                val relative = baseSource.relativize(path)
                val target = targetRoot.resolve(relative)
                Files.createDirectories(target.getParent)

                try {
                  var fileProgress = 0.0

                  strategy.process(path, target, replaceExisting, copyAttributes, progress => {
                    fileProgress = progress
                    val globalProgress = (i + fileProgress) / total
                    updateProgress(globalProgress, 1.0)
                  })

                  if (fileProgress == 0.0) {
                    updateProgress((i + 1.0) / total, 1.0)
                  }
                } catch {
                  case ex: Exception => log.error(ex.getMessage, ex)
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

          task.setOnFailed { _ =>
            progressDialog.close()
          }

          Platform.runLater {
            progressDialog.show()
          }
          new Thread(task).start()
        } else {
          val lockedDialog = new Dialog[ButtonType]() {
            title = "Locked Files Detected"
            headerText = "The following files are currently in use and cannot be processed:"
            dialogPane().buttonTypes = Seq(ButtonType.OK)
            val contentBox = new VBox(5)
            lockedFiles.foreach(f => contentBox.getChildren.add(new javafx.scene.control.Label(f.toString)))
            dialogPane().content = contentBox
            dialogPane().getStylesheets.add(
              getClass.getResource(s"/${if darkMode.value then "dark" else "light"}-theme.css").toExternalForm
            )
          }

          lockedDialog.showAndWait()
        }
      }
    }
  }

  def isFileLocked(path: Path): Boolean = {
    try {
      val channel = java.nio.channels.FileChannel.open(path)
      channel.close()
      false
    } catch {
      case _: java.nio.file.AccessDeniedException => true
      case _: java.io.IOException => true
    }
  }

  def copyFileWithProgress(source: Path,
                           target: Path,
                           progressCallback: Double => Unit
                          ): Unit = {
    Files.createDirectories(target.getParent)

    val totalBytes = Files.size(source)
    var copiedBytes: Long = 0
    val buffer = new Array[Byte](1024 * 1024)

    Using.resources(
      new BufferedInputStream(Files.newInputStream(source)),
      new BufferedOutputStream(Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))
    ) { (in, out) =>
      var bytesRead = in.read(buffer)
      while (bytesRead != -1) {
        out.write(buffer, 0, bytesRead)
        copiedBytes += bytesRead
        progressCallback(copiedBytes.toDouble / totalBytes)
        bytesRead = in.read(buffer)
      }
    }
  }

}

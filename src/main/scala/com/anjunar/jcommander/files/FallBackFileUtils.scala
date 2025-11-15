package com.anjunar.jcommander.files

import com.anjunar.jcommander.components.{DarkModeComponent, FileTableComponent}
import com.anjunar.jcommander.{Main, OSType, WinNativeCopy}
import com.anjunar.jcommander.CdiUtils.*
import com.anjunar.jcommander.ui.ThemedDialog
import com.typesafe.scalalogging.Logger
import jakarta.enterprise.context.ApplicationScoped
import javafx.concurrent
import scalafx.Includes.{jfxDialogPane2sfx, observableList2ObservableBuffer}
import scalafx.application.Platform
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.event.ActionEvent
import scalafx.scene.control.*
import scalafx.scene.layout.VBox

import java.awt.image.BufferedImage
import java.io.{BufferedInputStream, BufferedOutputStream, File}
import java.nio.file.{Files, Path, StandardCopyOption, StandardOpenOption}
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*
import scala.util.Using

class FallBackFileUtils extends AbstractFileUtils {

  override val log: Logger = Logger[FallBackFileUtils]

  override def fileContext(file: File): Unit = ???

  override def console(workingDir: File): Unit = ???

  override def getFileIcon(file: File, large: Boolean): BufferedImage = ???

  override def executeFile(file: File): Unit = ???

  override def copyFiles(activeTable: FileTableComponent, otherTable: FileTableComponent): Unit = {
    processFiles(
      (path: Path, target: Path, replaceExisting: Boolean, copyAttributes: Boolean, progressCallback: Double => Unit) => {
        val copyOption = copyOptions(replaceExisting, copyAttributes)

        copyFileWithProgress(path, target, replaceExisting, copyAttributes, progressCallback)
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
      (path: Path, target: Path, replaceExisting: Boolean, copyAttributes: Boolean, progressCallback: Double => Unit) => {
        val sameDrive =
          path.getRoot != null &&
            target.getRoot != null &&
            path.getRoot.equals(target.getRoot)

        val copyOption = copyOptions(replaceExisting, copyAttributes)

        if sameDrive then {
          Files.createDirectories(target.getParent)
          Files.move(path, target, copyOption *)
        } else
          copyFileWithProgress(path, target, replaceExisting, copyAttributes, progressCallback)
          setWriteable(path)
          Files.delete(path)
      },
      "Move Files",
      "Should the selected Files be moved?",
      "Moving Files...",
      false,
      activeTable,
      otherTable
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

  override def deleteFiles(activeTable: FileTableComponent, otherTable: FileTableComponent): Unit = {
    processFiles(
      (path: Path, target: Path, replaceExisting: Boolean, copyAttributes: Boolean, progressCallback: Double => Unit) => {
        setWriteable(path)
        Files.delete(path)
      },
      "Delete Files",
      "Should the selected Files be deleted?",
      "Deleting Files...",
      true,
      activeTable,
      otherTable
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

  def processFiles(strategy: FallBackFileStrategy,
                   confirmTitle: String,
                   confirmHeader: String,
                   progressText: String,
                   isDelete: Boolean,
                   activeTable: FileTableComponent,
                   otherTable: FileTableComponent): Unit = {

    val replaceExistingBox = new CheckBox("Replace existing files") {
      selected = true
    }

    val copyAttributesExistingBox = new CheckBox("Copying Attributes") {
      selected = false
    }

    val checkForLockedFilesBox = new CheckBox("Check for locked Files") {
      selected = false
    }

    val confirmDialog = new ThemedDialog[ButtonType]() {
      title = confirmTitle
      headerText = confirmTitle
      dialogPane.buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
      if (!isDelete) {
        if (OSType.osName == "win") {
          dialogPane.content = new VBox(10, replaceExistingBox)
        } else {
          dialogPane.content = new VBox(10, replaceExistingBox, copyAttributesExistingBox)
        }
      } else {
        dialogPane.content = new VBox(10, checkForLockedFilesBox)
      }
    }

    confirmDialog.resultConverter = btn => btn

    confirmDialog.showAndWaitDialog().foreach { result =>
      if (result == ButtonType.OK) {

        val replaceExisting = replaceExistingBox.selected.value
        val copyAttributes = copyAttributesExistingBox.selected.value
        val checkForLockedFiles = checkForLockedFilesBox.selected.value

        val lockedFiles = new ListBuffer[Path]
        val selectedItems = activeTable.node.selectionModel.value.getSelectedItems

        val allFiles = selectedItems.stream().flatMap { fileItem =>
          val path = fileItem.file.toPath
          if (Files.isDirectory(path))
            Files.walk(path).peek(path => {
              if (checkForLockedFiles && isFileLocked(path)) {
                lockedFiles.addOne(path)
              }
            }).sorted(java.util.Comparator.reverseOrder())
          else
            java.util.stream.Stream.of(path)
        }.toList.asScala.toSeq

        if (lockedFiles.isEmpty || !checkForLockedFiles) {

          val progressBar = new ProgressBar {
            prefWidth = 350
          }

          val progressLabel = new Label("0 MB copied (0 MB/s)")

          val task = new concurrent.Task[Unit]() {
            override def call(): Unit = {
              val total = allFiles.size
              val baseSource = selectedItems.head.file.toPath.getParent
              val targetRoot = otherTable.directory.toPath

              var totalBytesCopied: Long = 0
              val startTime = System.nanoTime()
              val totalBytes: Long = allFiles.map(path => if (Files.isRegularFile(path)) Files.size(path) else 0L).sum

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

                  val fileSize = if (Files.isRegularFile(path)) Files.size(path) else 0L
                  totalBytesCopied += fileSize

                  val elapsedSeconds = (System.nanoTime() - startTime) / 1e9
                  val mbCopied = totalBytesCopied / (1024.0 * 1024.0)
                  val mbTotal = totalBytes / (1024.0 * 1024.0)
                  val mbPerSec = if (elapsedSeconds > 0) mbCopied / elapsedSeconds else 0.0

                  val remainingBytes = totalBytes - totalBytesCopied
                  val etaSeconds = if (mbPerSec > 0) remainingBytes / (mbPerSec * 1024 * 1024) else 0.0
                  val etaText = f"${(etaSeconds / 60).toInt}%02d:${(etaSeconds % 60).toInt}%02d"

                  Platform.runLater {
                    progressLabel.setText(f"$mbCopied%.2f / $mbTotal%.2f MB (${mbPerSec}%.2f MB/s), ETA: $etaText")
                  }

                } catch {
                  case ex: Exception => log.error(ex.getMessage, ex)
                }

                updateProgress(i + 1, total)
              }
            }
          }

          val progressDialog = new ThemedDialog[Unit]() {
            title = progressText
            dialogPane.content = new VBox(10, progressBar, progressLabel)
            dialogPane.buttonTypes = Seq(ButtonType.Cancel)
          }

          progressDialog.dialogPane.lookupButton(ButtonType.Cancel).addEventFilter(ActionEvent.Action, _ => {
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
                           replaceExisting: Boolean,
                           copyAttributes: Boolean,
                           progressCallback: Double => Unit): Unit = {

    Files.createDirectories(target.getParent)

    if (Files.isRegularFile(source)) {

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
}

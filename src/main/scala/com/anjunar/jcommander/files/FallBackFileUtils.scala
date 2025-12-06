package com.anjunar.jcommander.files

import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.traits.HasText.{text, textProperty}
import com.anjunar.javafx.scene.control.{checkbox, label}
import com.anjunar.javafx.scene.control.checkbox.selectedProperty
import com.anjunar.javafx.stage.Window
import com.anjunar.jcommander.components.DarkModeComponent
import com.anjunar.jcommander.dsl.FileTable
import com.anjunar.jcommander.dsl.dialog.{ConfirmDialog, ProgressDialog}
import com.anjunar.jcommander.utils.CdiUtils.*
import com.anjunar.jcommander.utils.OSType
import com.anjunar.jcommander.{Main, WinNativeCopy}
import com.typesafe.scalalogging.Logger
import jakarta.enterprise.context.ApplicationScoped
import javafx.application.Platform
import javafx.beans.property.{SimpleBooleanProperty, SimpleStringProperty}
import javafx.concurrent
import javafx.scene.input.MouseEvent

import java.awt.image.BufferedImage
import java.io.{BufferedInputStream, BufferedOutputStream, File}
import java.nio.file.{Files, Path, StandardCopyOption, StandardOpenOption}
import java.util.concurrent.atomic.AtomicBoolean
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*
import scala.util.Using

class FallBackFileUtils extends AbstractFileUtils {

  override val log: Logger = Logger[FallBackFileUtils]

  private val fileUtils = FileUtilsProducer.produce()

  override def fileContext(files: Seq[String], event: MouseEvent): Unit = {
    OSType.osName match {
      case "win" =>
      case _ => fileUtils.fileContext(files, event)
    }
  }

  override def console(workingDir: String): Unit = fileUtils.console(workingDir)

  override def getFileIcon(file: String, large: Boolean): BufferedImage = fileUtils.getFileIcon(file, large)

  override def executeFile(file: String): Unit = fileUtils.executeFile(file)

  override def copyFiles(activeTable: FileTable, otherTable: FileTable): Unit = {
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

  override def moveFiles(activeTable: FileTable, otherTable: FileTable): Unit = {
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

  override def deleteFiles(activeTable: FileTable, otherTable: FileTable): Unit = {
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
                   activeTable: FileTable,
                   otherTable: FileTable): Unit = {

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

        val replaceExisting = replaceExistingBox.get()

        val lockedFiles = new ListBuffer[Path]
        val selectedItems = activeTable.node.getSelectionModel.getSelectedItems

        val allFiles = selectedItems.stream().flatMap { fileItem =>
          val path = Path.of(fileItem.file)
          if (Files.isDirectory(path))
            Files.walk(path).sorted(java.util.Comparator.reverseOrder())
          else
            java.util.stream.Stream.of(path)
        }.toList.asScala.toSeq

        if (lockedFiles.isEmpty) {

          val cancelledFlag = new AtomicBoolean(false)

          val progressString = new SimpleStringProperty()
          val fileString = new SimpleStringProperty()

          val task = new concurrent.Task[Unit]() {
            override def call(): Unit = {
              val total = allFiles.size
              val baseSource = selectedItems.getFirst.parent
              val targetRoot = Path.of(otherTable.directoryProperty.get())

              var totalBytesCopied: Long = 0
              val startTime = System.nanoTime()
              val totalBytes: Long = allFiles.map(path => if (Files.isRegularFile(path)) Files.size(path) else 0L).sum

              allFiles.zipWithIndex.foreach { case (path, i) =>
                if (isCancelled) return

                val relative = Path.of(baseSource).relativize(path)
                val target = targetRoot.resolve(relative)
                Files.createDirectories(target.getParent)

                try {
                  var fileProgress = 0.0

                  strategy.process(path, target, replaceExisting, false, progress => {
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

                  Platform.runLater { () =>
                    progressString.set(f"$mbCopied%.2f / $mbTotal%.2f MB (${mbPerSec}%.2f MB/s), ETA: $etaText")
                  }

                } catch {
                  case ex: Exception => log.error(ex.getMessage, ex)
                }

                updateProgress(i + 1, total)
              }
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

package com.anjunar.jcommander.files

import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.traits.HasText.{text, textProperty}
import com.anjunar.javafx.scene.control.{checkbox, label}
import com.anjunar.javafx.scene.control.checkbox.selectedProperty
import com.anjunar.javafx.stage.Window
import com.anjunar.jcommander.dsl.FileTable
import com.anjunar.jcommander.dsl.dialog.{ConfirmDialog, ProgressDialog}
import com.anjunar.jcommander.utils.OSType
import com.typesafe.scalalogging.Logger
import javafx.application.Platform
import javafx.beans.property.{SimpleBooleanProperty, SimpleStringProperty}
import javafx.concurrent
import javafx.scene.input.MouseEvent

import java.io.{BufferedInputStream, BufferedOutputStream, File}
import java.nio.file.{Files, Path, StandardCopyOption, StandardOpenOption}
import java.util.concurrent.atomic.AtomicBoolean
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*
import scala.util.Using

class FallBackFileUtils extends AbstractFileUtils, WinFallbackFileUtils {

  override val log: Logger = Logger[FallBackFileUtils]

  private val fileUtils = FileUtilsProducer.produceWithoutFallback()

  override def fileContext(files: Seq[String], event: MouseEvent): Unit = {
    OSType.osName match {
      case "win" => winFileContext(files, event)
      case _ => fileUtils.fileContext(files, event)
    }
  }

  override def executeFile(file: String): Unit = {
    OSType.osName match {
      case "win" => winExecuteFile(file)
      case _ => fileUtils.executeFile(file)
    }
  }

  override def console(workingDir: String): Unit = fileUtils.console(workingDir)

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

        if sameDrive then
          Files.createDirectories(target.getParent)
          Files.move(path, target, copyOption *)
        else
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
    if copyAttributes then
      copyOption = copyOption ++ Seq(StandardCopyOption.COPY_ATTRIBUTES)
    if replaceExisting then
      copyOption = copyOption ++ Seq(StandardCopyOption.REPLACE_EXISTING)
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
    if !path.toFile.canWrite then
      try
        path.toFile.setWritable(true)
      catch
        case ex: Exception => log.error(ex.getMessage, ex)
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
      if result == "Ok" then

        val replaceExisting = replaceExistingBox.get()

        val lockedFiles = new ListBuffer[Path]
        val selectedItems = activeTable.node.getSelectionModel.getSelectedItems

        val allFiles = selectedItems.stream().flatMap { fileItem =>
          val path = Path.of(fileItem.file)
          if Files.isDirectory(path) then
            Files.walk(path).sorted(java.util.Comparator.reverseOrder())
          else
            java.util.stream.Stream.of(path)
        }.toList.asScala.toSeq

        if lockedFiles.isEmpty then

          val cancelledFlag = new AtomicBoolean(false)

          val progressString = new SimpleStringProperty()
          val fileString = new SimpleStringProperty()

          val task = new concurrent.Task[Unit]() {
            override def call(): Unit = {
              val baseSource = selectedItems.getFirst.parent
              val targetRoot = Path.of(otherTable.directoryProperty.get())

              val totalBytes: Long =
                allFiles.map(path => if Files.isRegularFile(path) then Files.size(path) else 0L).sum

              val startTime = System.nanoTime()
              var processedBytesCompletedFiles: Long = 0L

              if totalBytes == 0L then
                Platform.runLater { () =>
                  progressString.set("0.00 / 0.00 MB (0.00 MB/s), ETA: 00:00")
                }

              allFiles.foreach { path =>
                if isCancelled then return

                val relative = Path.of(baseSource).relativize(path)
                val target = targetRoot.resolve(relative)
                Files.createDirectories(target.getParent)

                val fileSize = if Files.isRegularFile(path) then Files.size(path) else 0L

                Platform.runLater { () =>
                  fileString.set(relative.toString)
                }

                if fileSize > 0 && !isDelete then
                  try
                    strategy.process(path, target, replaceExisting, false, progress => {
                      if isCancelled then return

                      val clamped =
                        if progress.isNaN then 0.0
                        else math.max(0.0, math.min(1.0, progress))

                      val currentFileBytes = (fileSize.toDouble * clamped).toLong
                      val totalBytesCopied = processedBytesCompletedFiles + currentFileBytes

                      val elapsedSeconds = (System.nanoTime() - startTime) / 1e9
                      val bytesPerSec = if elapsedSeconds > 0 then totalBytesCopied / elapsedSeconds else 0.0
                      val mbCopied = totalBytesCopied / (1024.0 * 1024.0)
                      val mbTotal = totalBytes / (1024.0 * 1024.0)
                      val mbPerSec = bytesPerSec / (1024.0 * 1024.0)
                      val mbitPerSec = mbPerSec * 8.0

                      val remainingBytes = math.max(0L, totalBytes - totalBytesCopied)
                      val etaSeconds = if bytesPerSec > 0 then remainingBytes / bytesPerSec else 0.0
                      val etaMinutesPart = (etaSeconds / 60).toInt
                      val etaSecondsPart = (etaSeconds % 60).toInt
                      val etaText = f"$etaMinutesPart%02d:$etaSecondsPart%02d"

                      updateProgress(totalBytesCopied.toDouble, totalBytes.toDouble)

                      Platform.runLater { () =>
                        progressString.set(f"$mbCopied%.2f / $mbTotal%.2f MB ($mbPerSec%.2f MB/s), ETA: $etaText")
                      }
                    })
                  catch
                    case ex: Exception => log.error(ex.getMessage, ex)
                else
                  try
                    strategy.process(path, target, replaceExisting, false, _ => ())
                  catch
                    case ex: Exception => log.error(ex.getMessage, ex)

                processedBytesCompletedFiles += fileSize

                val totalBytesCopied = processedBytesCompletedFiles

                val elapsedSeconds = (System.nanoTime() - startTime) / 1e9
                val bytesPerSec = if elapsedSeconds > 0 then totalBytesCopied / elapsedSeconds else 0.0
                val mbCopied = totalBytesCopied / (1024.0 * 1024.0)
                val mbTotal = if totalBytes > 0 then totalBytes / (1024.0 * 1024.0) else 0.0
                val mbPerSec = if elapsedSeconds > 0 && totalBytes > 0 then bytesPerSec / (1024.0 * 1024.0) else 0.0
                val mbitPerSec = mbPerSec * 8.0

                val remainingBytes = math.max(0L, totalBytes - totalBytesCopied)
                val etaSeconds = if bytesPerSec > 0 then remainingBytes / bytesPerSec else 0.0
                val etaMinutesPart = (etaSeconds / 60).toInt
                val etaSecondsPart = (etaSeconds % 60).toInt
                val etaText = f"$etaMinutesPart%02d:$etaSecondsPart%02d"

                updateProgress(totalBytesCopied.toDouble, totalBytes.toDouble)

                Platform.runLater { () =>
                  progressString.set(f"$mbCopied%.2f / $mbTotal%.2f MB ($mbPerSec%.2f MB/s), ETA: $etaText")
                }
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

  def copyFileWithProgress(source: Path,
                           target: Path,
                           replaceExisting: Boolean,
                           copyAttributes: Boolean,
                           progressCallback: Double => Unit): Unit = {

    Files.createDirectories(target.getParent)

    if Files.isRegularFile(source) then

      val totalBytes = Files.size(source)
      var copiedBytes: Long = 0
      val buffer = new Array[Byte](1024 * 1024)

      val outOptions =
        if replaceExisting then
          Array(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        else
          Array(StandardOpenOption.CREATE_NEW)

      Using.resources(
        new BufferedInputStream(Files.newInputStream(source)),
        new BufferedOutputStream(Files.newOutputStream(target, outOptions *))
      ) { (in, out) =>
        var bytesRead = in.read(buffer)
        while bytesRead != -1 do
          out.write(buffer, 0, bytesRead)
          copiedBytes += bytesRead
          if totalBytes > 0 then
            progressCallback(copiedBytes.toDouble / totalBytes.toDouble)
          bytesRead = in.read(buffer)
      }

      if copyAttributes then
        try
          Files.setLastModifiedTime(target, Files.getLastModifiedTime(source))
        catch
          case ex: Exception => log.error(ex.getMessage, ex)
  }
}

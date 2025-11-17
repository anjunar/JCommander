package com.anjunar.jcommander.files

import com.anjunar.jcommander.components.{AbstractFileTableComponent, LocalFileTableComponent, VFS2FileTableComponent}
import com.anjunar.jcommander.ui.ThemedDialog
import com.anjunar.jcommander.utils.{ProgressListener, VFSUtils}
import org.apache.commons.vfs2.FileObject
import scalafx.event.ActionEvent
import scalafx.scene.control.{ButtonType, CheckBox, Label, ProgressBar, TextField}
import scalafx.scene.layout.VBox
import javafx.concurrent
import scalafx.Includes.observableList2ObservableBuffer
import scalafx.application.Platform

import java.awt.image.BufferedImage
import java.nio.file.Files
import scala.jdk.CollectionConverters.*

class StreamFileUtils extends FileUtils {

  override def fileContext(files: Seq[String]): Unit = throw new NotImplementedError("Will not be implemented in Future")

  override def getFileIcon(file: String, large: Boolean): BufferedImage = throw new NotImplementedError("Will not be implemented in Future")

  override def executeFile(file: String, workingDir: String, args: Seq[String]): Unit = throw new NotImplementedError("Will not be implemented in Future")

  override def console(workingDir: String): Unit = throw new NotImplementedError("Will not be implemented in Future")

  override def executeFile(file: String): Unit = throw new NotImplementedError("Will not be implemented in Future")

  override def mkDir(activeTable: AbstractFileTableComponent): Unit = {
    val textField: TextField = new TextField {
      promptText = "Directory Name"
    }

    val mkDirDialog = new ThemedDialog[ButtonType]() {
      title = "Create Directory"
      headerText = "Create Directory"
      dialogPane.buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
      dialogPane.content = new VBox(10, textField)
    }

    mkDirDialog.resultConverter = btn => btn

    mkDirDialog.showAndWaitDialog().foreach { result =>
      if (result == ButtonType.OK) {
        val newFileName = textField.text.value
        activeTable.resolveDirectory
          .resolveFile(newFileName)
          .createFolder()
      }
    }
  }

  override def renameFile(activeTable: AbstractFileTableComponent): Unit = {
    val textField = new TextField {
      text = activeTable.node.selectionModel.value.getSelectedItem.name
    }

    val renameDialog = new ThemedDialog[ButtonType]() {
      title = "Rename File"
      headerText = "Rename File"
      dialogPane.buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
      dialogPane.content = new VBox(10, textField)
    }

    renameDialog.resultConverter = btn => btn

    renameDialog.showAndWaitDialog().foreach { result =>
      if (result == ButtonType.OK) {
        val selected = activeTable.node.selectionModel.value.getSelectedItems.head
        val oldFile = activeTable.manager.resolveFile(selected.file)

        val parent = Option(oldFile.getParent).getOrElse {
          val parentName = oldFile.getName.getParent
          activeTable.manager.resolveFile(parentName.getBaseName)
        }

        val newFile = parent.resolveFile(textField.text.value)
        oldFile.moveTo(newFile)
      }
    }
  }

  override def copyFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit = {
    processFiles(
      (sources: Seq[FileObject], destDir: FileObject, listener: ProgressListener) => {
        VFSUtils.copyMultiple(sources, destDir, listener)
      },
      "Copy Files",
      "Should the selected Files be copied?",
      "Copying Files...",
      false,
      activeTable,
      otherTable
    )
  }

  override def moveFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit = {
    processFiles(
      (sources: Seq[FileObject], destDir: FileObject, listener: ProgressListener) => {
        VFSUtils.moveMultiple(sources, destDir, listener)
      },
      "Move Files",
      "Should the selected Files be moved?",
      "Moving Files...",
      false,
      activeTable,
      otherTable
    )
  }

  override def deleteFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit = {
    processFiles(
      (sources: Seq[FileObject], destDir: FileObject, listener: ProgressListener) => {
        VFSUtils.deleteMultiple(sources, listener)
      },
      "Delete Files",
      "Should the selected Files be deleted?",
      "Deleting Files...",
      true,
      activeTable,
      otherTable
    )
  }

  def processFiles(strategy: StreamFileStrategy,
                   confirmTitle: String,
                   confirmHeader: String,
                   progressText: String,
                   isDelete: Boolean,
                   activeTable: AbstractFileTableComponent,
                   otherTable: AbstractFileTableComponent): Unit = {

    val confirmDialog = new ThemedDialog[ButtonType]() {
      title = confirmTitle
      headerText = confirmHeader
      dialogPane.buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    }

    confirmDialog.resultConverter = btn => btn

    confirmDialog.showAndWaitDialog().foreach { result =>
      if (result == ButtonType.OK) {

        val progressBar = new ProgressBar {
          prefWidth = Double.MaxValue
        }
        val progressLabel = new Label("0 MB (0 MB/s)")
        val fileLabel = new Label("...Calculating")

        val task = new concurrent.Task[Unit]() {
          override def call(): Unit = {
            val selectedItems = activeTable.node.selectionModel.value.getSelectedItems
            val sources = selectedItems.stream().map(item => activeTable.manager.resolveFile(item.file)).toList.asScala.toSeq
            val target = otherTable.resolveDirectory

            @volatile var lastTimeNs: Long = System.nanoTime()
            @volatile var lastBytes: Long = 0L
            var speedEwma: Double = 0.0
            val alpha = 0.25

            val listener = new ProgressListener {
              override def onFileProgress(file: FileObject, bytes: Long, totalBytes: Long): Unit = {
                val nowNs = System.nanoTime()
                val deltaBytes = bytes - lastBytes
                val deltaTimeSec = (nowNs - lastTimeNs).toDouble / 1e9

                val instantSpeed = if (deltaTimeSec > 0) deltaBytes.toDouble / deltaTimeSec else 0.0

                if (deltaTimeSec > 0) {
                  speedEwma = if (speedEwma == 0.0) instantSpeed else (alpha * instantSpeed + (1 - alpha) * speedEwma)
                  lastTimeNs = nowNs
                  lastBytes = bytes
                }

                updateProgress(bytes, totalBytes)
                val bytesMb = bytes.toDouble / (1024 * 1024)
                val speedMb = speedEwma / (1024 * 1024)

                Platform.runLater {
                  progressLabel.text = f"${bytesMb}%.2f MB (${speedMb}%.2f MB/s)"
                  fileLabel.text = file.getPublicURIString
                }
              }
            }

            strategy.process(sources, target, (file: FileObject, bytes: Long, totalBytes: Long) => {
              listener.onFileProgress(file, bytes, totalBytes)
            })

            Platform.runLater {
              activeTable.loadDirectory(activeTable.directory)
              otherTable.loadDirectory(otherTable.directory)
            }
          }
        }

        val progressDialog = new ThemedDialog[Unit]() {
          title = progressText
          width = 1000
          dialogPane.content = new VBox(10, progressBar, progressLabel, fileLabel) {
            prefWidth = Double.MaxValue
          }
          dialogPane.buttonTypes = Seq(ButtonType.Cancel)
        }

        progressDialog.dialogPane.lookupButton(ButtonType.Cancel).addEventFilter(ActionEvent.Action, _ => {
          task.cancel()
          progressDialog.close()
        })

        progressBar.progress <== task.progressProperty()

        task.setOnSucceeded(_ => progressDialog.close())
        task.setOnFailed(_ => progressDialog.close())

        Platform.runLater {
          progressDialog.show()
        }
        new Thread(task).start()
      }
    }
  }
}
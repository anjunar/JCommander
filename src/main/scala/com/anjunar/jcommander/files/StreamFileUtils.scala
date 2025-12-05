package com.anjunar.jcommander.files

import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasText.{text, textProperty}
import com.anjunar.javafx.scene.control.{button, label}
import com.anjunar.javafx.scene.layout.hbox
import com.anjunar.javafx.scene.window.{close, closeWithResult}
import com.anjunar.javafx.scene.{header, window}
import com.anjunar.javafx.stage.Window
import com.anjunar.jcommander.components.{LocalFileTableComponent, VFS2FileTableComponent}
import com.anjunar.jcommander.dsl.RenameFileWindow.directoryName
import com.anjunar.jcommander.dsl.{FileTable, MakeDirectoryWindow, ProgressDialog, RenameFileWindow}
import com.anjunar.jcommander.ui.ThemedDialog
import com.anjunar.jcommander.utils.{ProgressListener, VFSUtils}
import com.typesafe.scalalogging.Logger
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.concurrent
import javafx.scene.input.MouseEvent
import org.apache.commons.vfs2.FileObject

import java.awt.image.BufferedImage
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicBoolean
import scala.jdk.CollectionConverters.*

class StreamFileUtils extends FileUtils {
  
  private val log = Logger[StreamFileUtils]

  override def fileContext(files: Seq[String], event: MouseEvent): Unit = throw new NotImplementedError("Will not be implemented in Future")

  override def getFileIcon(file: String, large: Boolean): BufferedImage = throw new NotImplementedError("Will not be implemented in Future")

  override def executeFile(file: String, workingDir: String, args: Seq[String]): Unit = throw new NotImplementedError("Will not be implemented in Future")

  override def console(workingDir: String): Unit = throw new NotImplementedError("Will not be implemented in Future")

  override def executeFile(file: String): Unit = throw new NotImplementedError("Will not be implemented in Future")

  override def mkDir(activeTable: FileTable): Unit = {

    val dialog = component[Window[String]] {
      MakeDirectoryWindow() {}
    }

    dialog.showAndWaitResult().foreach { result =>
      val newFileName = result
      activeTable.resolveDirectory
        .resolveFile(newFileName)
        .createFolder()
    }
  }

  override def renameFile(activeTable: FileTable): Unit = {

    val dialog = component[Window[String]] {
      RenameFileWindow() {
        directoryName = activeTable.node.getSelectionModel.getSelectedItem.name
      }
    }

    dialog.showAndWaitResult().foreach { result =>
      val selected = activeTable.node.getSelectionModel.getSelectedItems.getFirst
      val oldFile = activeTable.manager.resolveFile(selected.file)

      val parent = Option(oldFile.getParent).getOrElse {
        val parentName = oldFile.getName.getParent
        activeTable.manager.resolveFile(parentName.getBaseName)
      }

      val newFile = parent.resolveFile(result)
      oldFile.moveTo(newFile)
    }
  }

  override def copyFiles(activeTable: FileTable, otherTable: FileTable): Unit = {
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

  override def moveFiles(activeTable: FileTable, otherTable: FileTable): Unit = {
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

  override def deleteFiles(activeTable: FileTable, otherTable: FileTable): Unit = {
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
                   activeTable: FileTable,
                   otherTable: FileTable): Unit = {

    val confirmDialog = component[Window[String]] {
      window() {
        header() {
          label() {
            text = confirmTitle
          }
        }
        label() {
          text = confirmHeader
        }

        hbox() {
          button() {
            text = "Ok"
            onAction = _ => {
              closeWithResult("Ok")
            }
          }
          button() {
            text = "Cancel"
            onAction = _ => {
              close()
            }
          }
        }
      }
    }

    confirmDialog.showAndWaitResult().foreach { result =>
      if (result == "Ok") {

        val cancelledFlag = new AtomicBoolean(false)

        val progressString = new SimpleStringProperty()
        val fileString = new SimpleStringProperty()

        val task = new concurrent.Task[Unit]() {
          override def call(): Unit = {
            val selectedItems = activeTable.node.getSelectionModel.getSelectedItems
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

                Platform.runLater { () =>
                  progressString.set(f"${bytesMb}%.2f MB (${speedMb}%.2f MB/s)")
                  fileString.set(file.getPublicURIString)
                }
              }
            }

            strategy.process(sources, target, (file: FileObject, bytes: Long, totalBytes: Long) => {
              listener.onFileProgress(file, bytes, totalBytes)
            })

            Platform.runLater { () =>
              activeTable.loadDirectory(activeTable.directoryProperty.get())
              otherTable.loadDirectory(otherTable.directoryProperty.get())
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
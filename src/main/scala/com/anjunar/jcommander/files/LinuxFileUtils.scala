package com.anjunar.jcommander.files

import com.anjunar.jcommander.LinuxNativeCopy
import com.anjunar.jcommander.components.AbstractFileTableComponent
import com.anjunar.jcommander.ui.ThemedDialog
import javafx.concurrent
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.scene.control.{ButtonType, CheckBox, Label, ProgressBar}
import scalafx.scene.layout.VBox

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File}
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import javax.imageio.ImageIO
import scala.jdk.CollectionConverters.*
import scala.sys.process.*

class LinuxFileUtils extends AbstractFileUtils {

  override def console(workingDir: String): Unit = {
    val term = detectTerminal()
    val cmd = term match {
      case "gnome-terminal" => Seq("gnome-terminal", "--working-directory", workingDir)
      case "konsole" => Seq("konsole", "--workdir", workingDir)
      case "xfce4-terminal" => Seq("xfce4-terminal", "--working-directory", workingDir)
      case "xterm" => Seq("xterm")
      case _ => Seq("x-terminal-emulator")
    }
    new Thread(() => {
      cmd.!; ()
    }).start()
  }

  private def detectTerminal(): String =
    Seq("gnome-terminal", "konsole", "xfce4-terminal", "xterm")
      .find(t => Seq("which", t).!!.trim.nonEmpty)
      .getOrElse("x-terminal-emulator")

  override def executeFile(file: String): Unit =
    new Thread(() => {
      val out = new StringBuilder
      val log = ProcessLogger(line => out.append(line).append("\n"))
      val exit = Seq("xdg-open", file).!(log)
      if (exit != 0) {
        val msg = out.toString.trim match {
          case s if s.nonEmpty => s
          case _ => s"File could not be opened: $file"
        }
        Platform.runLater {
          val dlg = new ThemedDialog[Unit] {
            title = "Open Error"
            headerText = "The file could not be opened"
            dialogPane.buttonTypes = Seq(ButtonType.OK)
            dialogPane.content = new VBox(10, new Label(msg))
          }
          dlg.showAndWaitDialog()
        }
      }
    }).start()

  override def fileContext(files: Seq[String]): Unit = ???

  override def getFileIcon(file: String, large: Boolean): BufferedImage = {
    val bytes = LinuxNativeCopy.getFileIcon(file, large)
    ImageIO.read(new ByteArrayInputStream(bytes))
  }

  override def copyFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit = {
    processFiles(
      (paths: Seq[Path], target: Path, overwrite, recycle, ProgressListener: LinuxNativeCopy.ProgressListener) => {
        LinuxNativeCopy.copyFiles(paths.map(_.toAbsolutePath.toString).toArray, target.toAbsolutePath.toString, overwrite, ProgressListener)
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
      (paths: Seq[Path], target: Path, overwrite, recycle, ProgressListener: LinuxNativeCopy.ProgressListener) => {
        LinuxNativeCopy.moveFiles(paths.map(_.toAbsolutePath.toString).toArray, target.toAbsolutePath.toString, overwrite, ProgressListener)
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
      (paths: Seq[Path], target: Path, overwrite, recycle, ProgressListener: LinuxNativeCopy.ProgressListener) => {
        LinuxNativeCopy.deleteFiles(paths.map(_.toAbsolutePath.toString).toArray, recycle, ProgressListener)
      },
      "Delete Files",
      "Should the selected Files be deleted?",
      "Deleting Files...",
      true,
      activeTable,
      otherTable
    )
  }

  private def formatEta(seconds: Int): String = {
    if (seconds <= 0) "Calculating..."
    else if (seconds < 60) s"$seconds sec remaining"
    else {
      val minutes = seconds / 60
      val sec = seconds % 60
      if (minutes < 60) {
        if (sec == 0) s"$minutes min remaining"
        else s"$minutes min $sec sec remaining"
      } else {
        val hours = minutes / 60
        val min = minutes % 60
        if (min == 0) s"$hours h remaining"
        else s"$hours h $min min remaining"
      }
    }
  }

  def processFiles(
                    strategy: LinuxFileStrategy,
                    confirmTitle: String,
                    confirmHeader: String,
                    progressText: String,
                    isDelete: Boolean,
                    activeTable: AbstractFileTableComponent,
                    otherTable: AbstractFileTableComponent
                  ): Unit = {

    val replaceExistingBox = new CheckBox("Replace existing files") {
      selected = false
    }
    val moveToRecycleBinBox = new CheckBox("Move to Recycle Bin") {
      selected = true
    }

    val confirmDialog = new ThemedDialog[ButtonType] {
      title = confirmTitle
      headerText = confirmHeader
      dialogPane.buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
      dialogPane.content = new VBox(10) {
        if (!isDelete) {
          children += replaceExistingBox
        } else {
          children += moveToRecycleBinBox
        }
      }
    }

    confirmDialog.resultConverter = identity

    confirmDialog.showAndWaitDialog().foreach { result =>
      if (result == ButtonType.OK) {

        val overwriteExisting = replaceExistingBox.selected.value
        val moveToRecycleBin = moveToRecycleBinBox.selected.value

        val selectedFiles = activeTable.node.selectionModel.value.getSelectedItems.asScala.map(item => Path.of(item.file)).toSeq
        val targetDir = if isDelete then Path.of(activeTable.directory) else Path.of(otherTable.directory)

        val cancelledFlag = new AtomicBoolean(false)

        val progressBar = new ProgressBar {
          prefWidth = 350
        }
        val progressLabel = new Label("0% copied")
        val fileLabel = new Label("")

        val task = new concurrent.Task[Unit]() {
          override def call(): Unit = {
            val startTime = Instant.now()

            strategy.winProcess(
              selectedFiles,
              targetDir,
              overwriteExisting,
              moveToRecycleBin,
              new LinuxNativeCopy.ProgressListener {

                override def onFileProgress(operation: Int, source: String, target: String, bytesDone: Long, bytesTotal: Long): Unit = {
                  val percent = if bytesTotal == 0 then 0 else bytesDone.toDouble / bytesTotal
                  updateProgress(bytesDone, bytesTotal)
                  Platform.runLater {
                    progressLabel.setText(f"${percent * 100}%.0f%%")
                    fileLabel.setText(source)
                  }
                }

                override def onFileComplete(operation: Int, source: String, target: String): Unit = {}

                override def onComplete(operation: Int): Unit = {
                  Platform.runLater {
                    progressLabel.setText("Done")
                  }
                }

                override def onError(operation: Int, source: String, target: String, code: Int, message: String): Unit = {
                  Platform.runLater {
                    val dlg = new ThemedDialog[ButtonType] {
                      title = "Permission Required"
                      headerText = "Administrator privileges are required."
                      dialogPane.buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
                      dialogPane.content = new VBox(10, new Label(message))
                    }

                    dlg.showAndWaitDialog().foreach {
                      case ButtonType.OK =>
                        val cmd = operation match {
                          case 0 => Seq("pkexec", "cp", "-r", source, target)
                          case 1 => Seq("pkexec", "mv", source, target)
                          case 2 => Seq("pkexec", "rm", "-r", source)
                        }
                        new Thread(() => {
                          cmd.!; ()
                        }).start()

                      case _ =>
                    }
                  }
                }

                override def isCancelled: Boolean = cancelledFlag.get()
              }
            )
          }
        }

        val progressDialog = new ThemedDialog[Unit]() {
          title = progressText
          dialogPane.content = new VBox(10, progressBar, progressLabel, fileLabel)
          dialogPane.buttonTypes = Seq(ButtonType.Cancel)
        }

        val cancelButton = progressDialog.dialogPane.lookupButton(ButtonType.Cancel).asInstanceOf[javafx.scene.control.Button]
        cancelButton.addEventFilter(ActionEvent.Action, _ => {
          cancelledFlag.set(true)
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

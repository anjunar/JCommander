package com.anjunar.jcommander.files

import com.anjunar.javafx.dsl.{BuildContext, ElementBuilder, Ref}
import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasSpacing.{alignment, spacing, spacing_=}
import com.anjunar.javafx.dsl.traits.HasStyle.style
import com.anjunar.javafx.dsl.traits.HasText.{text, textProperty}
import com.anjunar.javafx.dsl.traits.HasGraphic.graphic
import com.anjunar.javafx.dsl.ChildBuilder.register
import com.anjunar.javafx.dsl.traits.HasPadding.padding
import com.anjunar.javafx.dsl.traits.HasWidth.prefWidth
import com.anjunar.javafx.dsl.traits.IsNode.vgrow
import com.anjunar.javafx.scene.control.checkbox.{selected, selectedProperty}
import com.anjunar.javafx.scene.control.progressBar.progressProperty
import com.anjunar.javafx.scene.control.{button, checkbox, contextMenu, label, menu, menuItem, progressBar, separatorMenuItem}
import com.anjunar.javafx.scene.layout.{hbox, region, vbox}
import com.anjunar.javafx.scene.window.{close, closeWithResult}
import com.anjunar.javafx.scene.{header, window}
import com.anjunar.javafx.stage.Window
import com.anjunar.jcommander.commands.{DeleteCommand, RenameCommand}
import com.anjunar.jcommander.dsl.Icon.{iconLiteral, iconSize}
import com.anjunar.jcommander.dsl.dialog.{ConfirmDialog, ProgressDialog, PropertiesDialog}
import com.anjunar.jcommander.dsl.{FileTable, Icon}
import com.anjunar.jcommander.utils.CdiUtils.inject
import com.anjunar.jcommander.LinuxNativeCopy
import javafx.application.Platform
import javafx.beans.property.{SimpleBooleanProperty, SimpleStringProperty}
import javafx.concurrent
import javafx.event.{ActionEvent, EventHandler}
import javafx.geometry.{Insets, Pos}
import javafx.scene.Node
import javafx.scene.control.{ContextMenu, Menu, MenuItem}
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{HBox, Priority}

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File}
import java.nio.file.attribute.{PosixFileAttributes, PosixFilePermissions}
import java.nio.file.{Files, Path, Paths}
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import javax.imageio.ImageIO
import scala.jdk.CollectionConverters.*
import scala.sys.process.*

class LinuxFileUtils extends AbstractFileUtils, UnixFileUtils {

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
      cmd.!;
      ()
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
        Platform.runLater { () =>

          val dlg: Window[Unit] = component[Window[Unit]] {
            window[Unit]() {
              header() {
                label() {
                  text = "Open Error"
                }
              }
              label() {
                text = "The file could not be opened"
              }
              label() {
                text = msg
              }
              hbox() {
                alignment = Pos.CENTER_RIGHT
                button() {
                  text = "Ok"
                  onAction = _ => {
                    close()
                  }
                }
              }
            }
          }

          dlg.showAndWaitResult()
        }
      }
    }).start()


  override def copyFiles(activeTable: FileTable, otherTable: FileTable): Unit = {
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

  override def moveFiles(activeTable: FileTable, otherTable: FileTable): Unit = {
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

  override def deleteFiles(activeTable: FileTable, otherTable: FileTable): Unit = {
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
                    activeTable: FileTable,
                    otherTable: FileTable
                  ): Unit = {

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

        val overwriteExisting = replaceExistingBox.get
        val moveToRecycleBin = moveToRecycleBinBox.get

        val selectedFiles = activeTable.node.getSelectionModel.getSelectedItems.asScala.map(item => Path.of(item.file)).toSeq
        val targetDir = if isDelete then Path.of(activeTable.directoryProperty.get()) else Path.of(otherTable.directoryProperty.get())

        val cancelledFlag = new AtomicBoolean(false)

        val progressString = new SimpleStringProperty()
        val fileString = new SimpleStringProperty()

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
                  Platform.runLater { () =>
                    progressString.set(f"${percent * 100}%.0f%%")
                    fileString.set(source)
                  }
                }

                override def onFileComplete(operation: Int, source: String, target: String): Unit = {}

                override def onComplete(operation: Int): Unit = {
                  Platform.runLater { () =>
                    progressString.set("Done")
                  }
                }

                override def onError(operation: Int, source: String, target: String, code: Int, message: String): Unit = {
                  val cmd = operation match {
                    case 0 => Seq("pkexec", "cp", "-r", source, target)
                    case 1 => Seq("pkexec", "mv", source, target)
                    case 2 => Seq("pkexec", "rm", "-r", source)
                  }
                  new Thread(() => {
                    cmd.!;
                    ()
                  }).start()
                }

                override def isCancelled: Boolean = cancelledFlag.get()
              }
            )
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

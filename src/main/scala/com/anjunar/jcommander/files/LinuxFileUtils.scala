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
import com.anjunar.jcommander.dsl.{FileTable, Icon, PropertiesDialog}
import com.anjunar.jcommander.ui.ThemedDialog
import com.anjunar.jcommander.utils.CdiUtils.inject
import com.anjunar.jcommander.{Icons, LinuxNativeCopy}
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

class LinuxFileUtils extends AbstractFileUtils {

  private var contextMenuOpen = false

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

  def ctxItem(text1: String, iconName: String)(action: EventHandler[ActionEvent])(using BuildContext, ElementBuilder[?]): MenuItem = {
    menuItem() {
      style = "-fx-padding: 4 10;"
      onAction = action
      graphic = hbox() {
        spacing = 10
        alignment = Pos.CENTER_LEFT
        style = "-fx-padding: 6 4 6 4;"
        Icon() {
          iconSize = 18
          iconLiteral = iconName
        }
        label() {
          style = "-fx-font-size: 13px;"
          text = text1
        }
      }
    }
  }

  def ctxSubMenu(text1: String, iconName: String, items: Seq[(BuildContext, ElementBuilder[Menu]) => MenuItem])(using BuildContext, ElementBuilder[?]): Menu = {
      menu() {
        graphic = component[HBox] {
          hbox() {
            spacing = 10
            alignment = Pos.CENTER_LEFT
            style = "-fx-padding: 6 4 6 4;"
            Icon() {
              iconSize = 18
              iconLiteral = iconName
            }
            label() {
              style = "-fx-font-size: 13px;"
              text = text1
            }
          }
        }
        items.foreach(item => item(summon[BuildContext], summon[ElementBuilder[Menu]]))
      }
  }

  override def fileContext(files: Seq[String], event: MouseEvent): Unit = {
    if (files.isEmpty) return
    if (contextMenuOpen) return

    val single = files.size == 1
    val fHead = files.head
    val parentDir = Path.of(fHead).getParent.toString


    val openWithApps = Seq("subl", "code", "gedit", "nano", "vim", "xdg-open")

    val menu = component[ContextMenu] {
      contextMenu() {
        if single then ctxItem("Execute", "mdi2p-play")(_ => {
          executeFile(fHead)
        })
        ctxSubMenu(
          "Open With…",
          "mdi2o-open-in-new",
          openWithApps.map(app =>
            (elementBuilder, buildContext) => ctxItem(app, "mdi2a-application")(_ => {
              new Thread(() => {
                Seq(app, fHead).!
                ()
              }).start()
            })(using elementBuilder, buildContext)
          )
        )
        separatorMenuItem() {}
        ctxItem("Copy", "mdi2c-content-copy")(_ => {
          FileClipboard.copyMany(files)
        })
        ctxItem("Paste", "mdi2c-content-paste")(_ => {
          FileClipboard.pasteToDirectory(parentDir)
        })
        if single then
          ctxItem("Rename", "mdi2r-rename-box")(_ => {
            val command = inject(classOf[RenameCommand])
            command.execute()
          })
        ctxItem("Duplicate", "mdi2c-content-copy")(_ => {
          new Thread(() => duplicateFiles(files)).start()
        })
        ctxItem("Delete", "mdi2d-delete")(_ => {
          val command = inject(classOf[DeleteCommand])
          command.execute()
        })
        separatorMenuItem() {}
        ctxSubMenu(
          "Compress",
          "mdi2z-zip-box",
          Seq(
            (elementBuilder, buildContext) => ctxItem("Create tar.gz", "mdi2f-folder-download")(_ => {
              new Thread(() => {
                val dir = new File(files.head).getParent
                val names = files.map(f => new File(f).getName)
                val cmd = Seq("tar", "-czf", s"$dir/archive.tar.gz", "-C", dir) ++ names
                cmd.!
                ()
              }).start()
            })(using elementBuilder, buildContext),
            (elementBuilder, buildContext) => ctxItem("Create zip", "mdi2z-zip-box")(_ => {
              new Thread(() => {
                val dir = new File(files.head).getParent
                val names = files.map(f => new File(f).getName)
                val cmd = Seq("zip", "-j", s"$dir/archive.zip") ++ names.map(n => s"$dir/$n")
                cmd.!
                ()
              }).start()
            })(using elementBuilder, buildContext)
          )
        )
        ctxItem("Extract here", "mdi2f-folder-open")(_ => {
          new Thread(() => extractFiles(files)).start()
        })
        ctxItem("Create symlink", "mdi2l-link")(_ => {
          new Thread(() => createSymlink(files)).start()
        })
        separatorMenuItem() {}
        ctxItem("Open Terminal Here", "mdi2c-console")(_ => {
          new Thread(() => console(Paths.get(files.head).getParent.toAbsolutePath.toString)).start()
        })
        ctxItem("Properties", "mdi2c-cog")(_ => {
          showPropertiesDialog(files)
        })
      }
    }

    Platform.runLater { () =>
      val node = event.getSource.asInstanceOf[Node]
      val scene = node.getScene

      contextMenuOpen = true

      val closer = new javafx.event.EventHandler[javafx.scene.input.MouseEvent] {
        override def handle(ev: javafx.scene.input.MouseEvent): Unit = {
          if (ev.isPrimaryButtonDown) menu.hide()
        }
      }

      scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, closer)

      menu.setOnHidden(_ => {
        scene.removeEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, closer)
        contextMenuOpen = false
      })

      menu.show(node, event.getScreenX, event.getScreenY)
    }
  }


  def showOpenWithDialog(files: Seq[String]): Unit = {
    val apps = Seq("subl", "code", "gedit", "nano", "vim", "xdg-open")

    val dlg = component[Window[Unit]] {
      window() {
        header() {
          label() {
            text = "Open With"
          }
        }

        label() {
          text = files.mkString(", ")
        }

        vbox() {
          apps.foreach { app => {
            button() {
              text = app
              onAction = _ => {
                new Thread(() => {
                  Seq(app, files.head).!
                  ()
                }).start()
                close()
              }
            }
          }}
        }

      }
    }


    dlg.showAndWaitResult()
  }

  def duplicateFiles(files: Seq[String]): Unit =
    new Thread(() => {
      files.foreach { p =>
        val path = Path.of(p)
        val target = path.getParent.resolve(path.getFileName.toString + "_copy")
        Seq("cp", "-r", p, target.toString).!
      }
      ()
    }).start()

  def extractFiles(files: Seq[String]): Unit =
    new Thread(() => {
      files.foreach { f =>
        if (f.endsWith(".zip")) Seq("unzip", f).!
        if (f.endsWith(".tar.gz")) Seq("tar", "-xzf", f).!
      }
      ()
    }).start()

  def createSymlink(files: Seq[String]): Unit =
    new Thread(() => {
      files.foreach { f =>
        val path = Path.of(f)
        val link = path.getParent.resolve(path.getFileName.toString + ".link")
        Seq("ln", "-s", f, link.toString).!
      }
      ()
    }).start()

  def showPropertiesDialog(files: Seq[String]): Unit = {
    val dialog = component[Window[Unit]] {
      PropertiesDialog(files) {

      }
    }

    dialog.showAndWaitResult()
  }

  override def getFileIcon(file: String, large: Boolean): BufferedImage = {
    val bytes = LinuxNativeCopy.getFileIcon(file, large)
    ImageIO.read(new ByteArrayInputStream(bytes))
  }

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
      window() {
        header() {
          label() {
            text = confirmTitle
          }
        }
        vbox() {
          spacing = 12
          padding = new Insets(10)

          label() {
            text = confirmHeader
          }

          if (isDelete) {
            checkbox() {
              text = "Move to Recycle Bin"
              selectedProperty.bindBidirectional(moveToRecycleBinBox)
            }
          } else {
            checkbox() {
              text = "Replace existing files"
              selectedProperty.bindBidirectional(replaceExistingBox)
            }
          }

          region() {
            vgrow = Priority.ALWAYS
          }

          hbox() {
            spacing = 10
            alignment = Pos.CENTER_RIGHT

            button() {
              text = "Cancel"
              onAction = _ => closeWithResult("Cancel")
            }
            button() {
              text = "OK"
              onAction = _ => closeWithResult("Ok")
            }
          }
        }
      }
    }

    confirmDialog.showAndWaitResult().foreach { result =>
      if (result == "Ok") {

        val overwriteExisting = replaceExistingBox.get
        val moveToRecycleBin = moveToRecycleBinBox.get

        val selectedFiles = activeTable.node.getSelectionModel.getSelectedItems.asScala.map(item => Path.of(item.file)).toSeq
        val targetDir = if isDelete then Path.of(activeTable.directory) else Path.of(otherTable.directory)

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
          window() {
            header() {
              label() {
                text = "Progress"
              }
            }

            vbox() {
              spacing = 14
              padding = new Insets(20)

              label() {
                text = progressText
              }

              progressBar() {
                prefWidth = 380
                progressProperty.bind(task.progressProperty())
              }

              label() {
                textProperty(prop => prop.bindBidirectional(progressString))
              }

              label() {
                textProperty(prop => prop.bindBidirectional(fileString))
              }

              region() {
                vgrow = Priority.ALWAYS
              }

              hbox() {
                alignment = Pos.CENTER_RIGHT
                button() {
                  text = "Cancel"
                  onAction = _ => {
                    cancelledFlag.set(true)
                    task.cancel()
                    close()
                    log.info("Operation cancelledFlag by user.")
                  }
                }
              }
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

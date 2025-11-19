package com.anjunar.jcommander.files

import com.anjunar.jcommander.{Icons, LinuxNativeCopy}
import com.anjunar.jcommander.components.AbstractFileTableComponent
import com.anjunar.jcommander.ui.ThemedDialog
import javafx.concurrent
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.geometry.Pos
import scalafx.scene.control.{Button, ButtonType, CheckBox, ContextMenu, Label, MenuItem, ProgressBar, SeparatorMenuItem, TextField}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.Includes.*
import scalafx.scene.Node
import scalafx.scene.input.MouseEvent

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File}
import java.nio.file.attribute.PosixFilePermissions
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

  def ctxItem(text: String, iconName: String)(action: => Unit): MenuItem = {
    val icon = Icons.themedIcon(iconName, 18)

    val lbl = new Label(text) {
      style = "-fx-font-size: 13px;"
    }

    val box = new HBox(10, icon, lbl) {
      alignment = Pos.CenterLeft
      style = "-fx-padding: 6 4 6 4;"
    }

    new MenuItem:
      graphic = box
      // schöner Hover-Effekt
      this.setStyle("-fx-padding: 4 10;")

      this.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, _ => {
        this.setStyle("-fx-background-color: -fx-accent; -fx-text-fill: white; -fx-padding: 4 10;")
      })
      this.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, _ => {
        this.setStyle("-fx-background-color: transparent; -fx-padding: 4 10;")
      })

      onAction = _ => action
  }

  override def fileContext(files: Seq[String], event: MouseEvent): Unit = {
    if (files.isEmpty) return
    if (contextMenuOpen) return // verhindert mehrfaches Öffnen

    val single = files.size == 1

    val menu = new ContextMenu()
    menu.getItems.addAll(
      ctxItem("Execute", "mdi2p-play") {
        if single then executeFile(files.head)
      },
      ctxItem("Open With…", "mdi2o-open-in-new") {
        showOpenWithDialog(files)
      },
      new SeparatorMenuItem,
      ctxItem("Copy", "mdi2c-content-copy") {
        FileClipboard.copyMany(files)
      },
      ctxItem("Paste", "mdi2c-content-paste") {
        val dir = Path.of(files.head).getParent.toString
        FileClipboard.pasteToDirectory(dir)
      },
      ctxItem("Rename", "mdi2r-rename-box") {
        if single then showRenameDialog(files)
      },
      ctxItem("Duplicate", "mdi2c-content-copy") {
        duplicateFiles(files)
      },
      ctxItem("Delete", "mdi2d-delete") {
        deleteFiles(files)
      },
      new SeparatorMenuItem,
      ctxItem("Compress", "mdi2z-zip-box") {
        showCompressDialog(files)
      },
      ctxItem("Extract", "mdi2f-folder-open") {
        extractFiles(files)
      },
      ctxItem("Create Symlink", "mdi2l-link") {
        createSymlink(files)
      },
      new SeparatorMenuItem,
      ctxItem("Open Terminal Here", "mdi2c-console") {
        openTerminalHere(Path.of(files.head).getParent.toString)
      },
      ctxItem("Properties", "mdi2c-cog") {
        showPropertiesDialog(files)
      }
    )

    Platform.runLater {
      val node = event.source.asInstanceOf[javafx.scene.Node]
      val scene = node.getScene

      contextMenuOpen = true

      val closer = new javafx.event.EventHandler[javafx.scene.input.MouseEvent] {
        override def handle(ev: javafx.scene.input.MouseEvent): Unit = {
          if (ev.isPrimaryButtonDown) {
            menu.hide()
          }
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
    val list = new VBox(10)
    list.children = apps.map(a => new Button(a))

    val dlg = new ThemedDialog[Unit] {
      title = "Open With"
      headerText = files.mkString(", ")
      dialogPane.buttonTypes = Seq(ButtonType.Close)
      dialogPane.content = list
    }

    apps.foreach { a =>
      list.children.find(_.asInstanceOf[javafx.scene.control.Button].getText == a).foreach { node =>
        node.asInstanceOf[javafx.scene.control.Button].setOnAction(_ => {
          new Thread(() => {
            Seq(a, files.head).!
            ()
          }).start()
          dlg.close()
        })
      }
    }

    dlg.showAndWaitDialog()
  }

  def showRenameDialog(files: Seq[String]): Unit = {
    if (files.size != 1) return
    val f = files.head
    val name = new TextField {
      text = Path.of(f).getFileName.toString
    }
    val apply = new Button("Rename")

    val dlg = new ThemedDialog[Unit] {
      title = "Rename"
      headerText = f
      dialogPane.buttonTypes = Seq(ButtonType.Close)
      dialogPane.content = new VBox(10, name, apply)
    }

    apply.onAction = _ => {
      new Thread(() => {
        val parent = Path.of(f).getParent.toString
        val target = parent + "/" + name.text.value
        Seq("mv", f, target).!
        ()
      }).start()
      dlg.close()
    }

    dlg.showAndWaitDialog()
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

  def deleteFiles(files: Seq[String]): Unit =
    new Thread(() => {
      (Seq("pkexec", "rm", "-r") ++ files).!;()
    }).start()

  def showCompressDialog(files: Seq[String]): Unit = {
    val tarBtn = new Button("tar.gz")
    val zipBtn = new Button("Zip")
    val box = new VBox(10, tarBtn, zipBtn)

    val dlg = new ThemedDialog[Unit] {
      title = "Compress"
      headerText = s"${files.size} files"
      dialogPane.buttonTypes = Seq(ButtonType.Close)
      dialogPane.content = box
    }

    tarBtn.onAction = _ => {
      new Thread(() => {
        (Seq("tar", "-czf", "archive.tar.gz") ++ files).!; ()
      }).start()
      dlg.close()
    }

    zipBtn.onAction = _ => {
      new Thread(() => {
        (Seq("zip", "-r", "archive.zip") ++ files).!; ()
      }).start()
      dlg.close()
    }

    dlg.showAndWaitDialog()
  }

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

  def openTerminalHere(dir: String): Unit = {
    new Thread(() => {
      Seq("gnome-terminal", "--working-directory", dir).!
      ()
    }).start()
  }

  def readPermissions(path: String): String = {
    val perms = Files.getPosixFilePermissions(Paths.get(path))
    PosixFilePermissions.toString(perms)
  }

  def rwxToOctal(perm: String): String = {
    def v(c: Char, b: Int) = if (c != '-') b else 0

    val u = v(perm(0), 4) + v(perm(1), 2) + v(perm(2), 1)
    val g = v(perm(3), 4) + v(perm(4), 2) + v(perm(5), 1)
    val o = v(perm(6), 4) + v(perm(7), 2) + v(perm(8), 1)

    s"$u$g$o"
  }

  def showPropertiesDialog(files: Seq[String]): Unit = {
    val label = if (files.size == 1) new File(files.head).getName else s"${files.size} files"

    val ownerR = new CheckBox("Owner Read")
    val ownerW = new CheckBox("Owner Write")
    val ownerX = new CheckBox("Owner Exec")

    val groupR = new CheckBox("Group Read")
    val groupW = new CheckBox("Group Write")
    val groupX = new CheckBox("Group Exec")

    val otherR = new CheckBox("Other Read")
    val otherW = new CheckBox("Other Write")
    val otherX = new CheckBox("Other Exec")

    val octal = new TextField {
      text = "755"
    }
    val apply = new Button("Apply")

    def applyPermissionsToChecks(perm: String): Unit = {
      ownerR.selected = perm.charAt(0) == 'r'
      ownerW.selected = perm.charAt(1) == 'w'
      ownerX.selected = perm.charAt(2) == 'x'

      groupR.selected = perm.charAt(3) == 'r'
      groupW.selected = perm.charAt(4) == 'w'
      groupX.selected = perm.charAt(5) == 'x'

      otherR.selected = perm.charAt(6) == 'r'
      otherW.selected = perm.charAt(7) == 'w'
      otherX.selected = perm.charAt(8) == 'x'
    }

    if (files.size == 1) {
      try {
        val permStr = readPermissions(files.head) // rwxr-xr--
        val oct = rwxToOctal(permStr) // 754
        octal.text = oct
        applyPermissionsToChecks(permStr)
      } catch {
        case e: Throwable => println("Could not read permissions: " + e.getMessage)
      }
    }

    def updateFromChecks(): Unit = {
      val o = (ownerR.selected(), ownerW.selected(), ownerX.selected()) match {
        case (true, true, true) => 7
        case (true, true, false) => 6
        case (true, false, true) => 5
        case (true, false, false) => 4
        case (false, true, true) => 3
        case (false, true, false) => 2
        case (false, false, true) => 1
        case _ => 0
      }

      val g = (groupR.selected(), groupW.selected(), groupX.selected()) match {
        case (true, true, true) => 7
        case (true, true, false) => 6
        case (true, false, true) => 5
        case (true, false, false) => 4
        case (false, true, true) => 3
        case (false, true, false) => 2
        case (false, false, true) => 1
        case _ => 0
      }

      val ot = (otherR.selected(), otherW.selected(), otherX.selected()) match {
        case (true, true, true) => 7
        case (true, true, false) => 6
        case (true, false, true) => 5
        case (true, false, false) => 4
        case (false, true, true) => 3
        case (false, true, false) => 2
        case (false, false, true) => 1
        case _ => 0
      }

      octal.text = s"$o$g$ot"
    }

    def updateChecksFromOctal(): Unit = {
      if (octal.text.value.matches("[0-7]{3}")) {
        val o = octal.text.value.charAt(0) - '0'
        val g = octal.text.value.charAt(1) - '0'
        val ot = octal.text.value.charAt(2) - '0'

        ownerR.selected = (o & 4) != 0
        ownerW.selected = (o & 2) != 0
        ownerX.selected = (o & 1) != 0

        groupR.selected = (g & 4) != 0
        groupW.selected = (g & 2) != 0
        groupX.selected = (g & 1) != 0

        otherR.selected = (ot & 4) != 0
        otherW.selected = (ot & 2) != 0
        otherX.selected = (ot & 1) != 0
      }
    }

    Seq(
      ownerR, ownerW, ownerX,
      groupR, groupW, groupX,
      otherR, otherW, otherX
    ).foreach(_.selected.onChange { (_, _, _) =>
      updateFromChecks()
    })

    octal.text.onChange { (_, _, _) =>
      updateChecksFromOctal()
    }

    val box = new VBox(10,
      new Label(label),
      new Label("Owner"), ownerR, ownerW, ownerX,
      new Label("Group"), groupR, groupW, groupX,
      new Label("Other"), otherR, otherW, otherX,
      new Label("Octal"), octal,
      apply
    )

    val dlg = new ThemedDialog[Unit] {
      title = "Properties"
      headerText = label
      dialogPane.buttonTypes = Seq(ButtonType.Close)
      dialogPane.content = box
    }

    apply.onAction = _ => {
      new Thread(() => {
        files.foreach { f =>
          Seq("pkexec", "chmod", octal.text.value, f).!
        }
        ()
      }).start()
      dlg.close()
    }

    dlg.showAndWaitDialog()
  }


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

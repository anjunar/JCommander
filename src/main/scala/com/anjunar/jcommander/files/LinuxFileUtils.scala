package com.anjunar.jcommander.files

import com.anjunar.jcommander.commands.{DeleteCommand, RenameCommand}
import com.anjunar.jcommander.dsl.FileTable
import com.anjunar.jcommander.ui.ThemedDialog
import com.anjunar.jcommander.utils.CdiUtils.inject
import com.anjunar.jcommander.{Icons, LinuxNativeCopy}
import javafx.concurrent
import javafx.scene.input.MouseEvent
import scalafx.Includes.*
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Node
import scalafx.scene.control.*
import scalafx.scene.layout.{GridPane, HBox, VBox}

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

  def ctxSubMenu(text: String, iconName: String, items: Seq[MenuItem]): Menu = {
    val icon = Icons.themedIcon(iconName, 18)
    val lbl = new Label(text) {
      style = "-fx-font-size: 13px;"
    }

    val box = new HBox(10, icon, lbl) {
      alignment = Pos.CenterLeft
      style = "-fx-padding: 6 4 6 4;"
    }

    val m = new Menu()
    m.setGraphic(box)
    m.getItems.addAll(items.map(_.delegate): _*)
    m
  }

  override def fileContext(files: Seq[String], event: MouseEvent): Unit = {
    if (files.isEmpty) return
    if (contextMenuOpen) return

    val single = files.size == 1
    val fHead = files.head
    val parentDir = Path.of(fHead).getParent.toString

    val menu = new ContextMenu()

    val openWithApps = Seq("subl", "code", "gedit", "nano", "vim", "xdg-open")
    val openWithMenu = ctxSubMenu(
      "Open With…",
      "mdi2o-open-in-new",
      openWithApps.map(app =>
        ctxItem(app, "mdi2a-application") {
          new Thread(() => {
            Seq(app, fHead).!
            ()
          }).start()
        }
      )
    )

    val compressMenu = ctxSubMenu(
      "Compress",
      "mdi2z-zip-box",
      Seq(
        ctxItem("Create tar.gz", "mdi2f-folder-download") {
          new Thread(() => {
            val dir = new File(files.head).getParent
            val names = files.map(f => new File(f).getName)
            val cmd = Seq("tar", "-czf", s"$dir/archive.tar.gz", "-C", dir) ++ names
            cmd.!
            ()
          }).start()
        },
        ctxItem("Create zip", "mdi2z-zip-box") {
          new Thread(() => {
            val dir = new File(files.head).getParent
            val names = files.map(f => new File(f).getName)
            val cmd = Seq("zip", "-j", s"$dir/archive.zip") ++ names.map(n => s"$dir/$n")
            cmd.!
            ()
          }).start()
        }
      )
    )

    val executeItem =
      ctxItem("Execute", "mdi2p-play") {
        if single then executeFile(fHead)
      }

    val copyItem =
      ctxItem("Copy", "mdi2c-content-copy") {
        FileClipboard.copyMany(files)
      }

    val pasteItem =
      ctxItem("Paste", "mdi2c-content-paste") {
        FileClipboard.pasteToDirectory(parentDir)
      }

    val renameItem =
      if single then
        ctxItem("Rename", "mdi2r-rename-box") {
          val command = inject(classOf[RenameCommand])
          command.execute()
        }
      else null

    val duplicateItem =
      ctxItem("Duplicate", "mdi2c-content-copy") {
        new Thread(() => duplicateFiles(files)).start()
      }

    val deleteItem =
      ctxItem("Delete", "mdi2d-delete") {
        val command = inject(classOf[DeleteCommand])
        command.execute()
      }

    val extractItem =
      ctxItem("Extract here", "mdi2f-folder-open") {
        new Thread(() => extractFiles(files)).start()
      }

    val symlinkItem =
      ctxItem("Create symlink", "mdi2l-link") {
        new Thread(() => createSymlink(files)).start()
      }

    val terminalItem =
      ctxItem("Open Terminal Here", "mdi2c-console") {
        new Thread(() => console(Paths.get(files.head).getParent.toAbsolutePath.toString)).start()
      }

    val propertiesItem =
      ctxItem("Properties", "mdi2c-cog") {
        showPropertiesDialog(files)
      }


    // --- BUILD MENU ---
    val items = Seq(
      executeItem,
      openWithMenu,
      new SeparatorMenuItem(),
      copyItem,
      pasteItem,
      renameItem,
      duplicateItem,
      deleteItem,
      new SeparatorMenuItem(),
      compressMenu,
      extractItem,
      symlinkItem,
      new SeparatorMenuItem(),
      terminalItem,
      propertiesItem
    ).filter(_ != null)

    menu.getItems.addAll(items.map(_.delegate): _*)



    // --- SHOW MENU ---
    Platform.runLater {
      val node = event.source.asInstanceOf[javafx.scene.Node]
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

    val ownerR = new CheckBox("r")
    val ownerW = new CheckBox("w")
    val ownerX = new CheckBox("x")

    val groupR = new CheckBox("r")
    val groupW = new CheckBox("w")
    val groupX = new CheckBox("x")

    val otherR = new CheckBox("r")
    val otherW = new CheckBox("w")
    val otherX = new CheckBox("x")

    val octal = new TextField {
      text = ""
    }
    val ownerField = new TextField {
      promptText = "owner"
    }
    val groupField = new TextField {
      promptText = "group"
    }
    val recursive = new CheckBox("Apply to subdirectories")
    val apply = new Button("Apply")

    def readAllPermissions(files: Seq[String]): Seq[String] = files.map(readPermissions)

    def setState(cb: CheckBox, values: Seq[Boolean]): Unit = {
      cb.allowIndeterminate = true
      if (values.forall(_ == true)) {
        cb.indeterminate = false
        cb.selected = true
      } else if (values.forall(_ == false)) {
        cb.indeterminate = false
        cb.selected = false
      } else {
        cb.indeterminate = true
      }
    }

    def applyMixedPermissions(perms: Seq[String]): Unit = {
      def col(i: Int): Seq[Boolean] = perms.map(_.charAt(i) != '-')

      setState(ownerR, col(0))
      setState(ownerW, col(1))
      setState(ownerX, col(2))
      setState(groupR, col(3))
      setState(groupW, col(4))
      setState(groupX, col(5))
      setState(otherR, col(6))
      setState(otherW, col(7))
      setState(otherX, col(8))
    }

    def applyPermissionsToChecks(perm: String): Unit = {
      Seq(ownerR, ownerW, ownerX,
        groupR, groupW, groupX,
        otherR, otherW, otherX).foreach { cb =>
        cb.allowIndeterminate = true
        cb.indeterminate = false
      }

      ownerR.selected = perm.charAt(0) == 'r'
      ownerW.selected = perm.charAt(1) == 'w'
      ownerX.selected = perm.charAt(2) == 'x'

      groupR.selected = perm.charAt(3) == 'r'
      groupW.selected = perm.charAt(4) == 'w'
      groupX.selected = perm.charAt(5) == 'x'

      otherR.selected = perm.charAt(6) == 'r'
      otherW.selected = perm.charAt(7) == 'w'
      otherX.selected = perm.charAt(8) == 'x'

      updateFromChecks()
    }

    if (files.size == 1) {
      val p = readPermissions(files.head)
      applyPermissionsToChecks(p)
      octal.text = rwxToOctal(p)
      val path = Paths.get(files.head)
      val attrs = Files.readAttributes(path, classOf[PosixFileAttributes])
      ownerField.text = attrs.owner().getName
      groupField.text = attrs.group().getName
    } else {
      val perms = readAllPermissions(files)
      applyMixedPermissions(perms)

      val owners = files.map { f =>
        val p = Paths.get(f)
        val attrs = Files.readAttributes(p, classOf[PosixFileAttributes])
        attrs.owner().getName
      }

      val groups = files.map { f =>
        val p = Paths.get(f)
        val attrs = Files.readAttributes(p, classOf[PosixFileAttributes])
        attrs.group().getName
      }

      if (owners.distinct.size == 1)
        ownerField.text = owners.head
      else
        ownerField.text = ""

      if (groups.distinct.size == 1)
        groupField.text = groups.head
      else
        groupField.text = ""

      octal.text = ""
    }

    def installMixedFix(cb: CheckBox): Unit = {
      cb.onAction = _ => {
        if (cb.indeterminate()) {
          cb.indeterminate = false
          cb.selected = true
        }
        updateFromChecks()
      }
    }

    Seq(ownerR, ownerW, ownerX,
      groupR, groupW, groupX,
      otherR, otherW, otherX).foreach(installMixedFix)

    def updateFromChecks(): Unit = {
      if (Seq(ownerR, ownerW, ownerX,
        groupR, groupW, groupX,
        otherR, otherW, otherX).exists(_.indeterminate())) {
        octal.text = ""
        return
      }

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

    octal.text.onChange { (_, _, _) =>
      if (octal.text.value.matches("[0-7]{3}")) {
        val o = octal.text.value.charAt(0) - '0'
        val g = octal.text.value.charAt(1) - '0'
        val ot = octal.text.value.charAt(2) - '0'

        ownerR.indeterminate = false
        ownerW.indeterminate = false
        ownerX.indeterminate = false
        groupR.indeterminate = false
        groupW.indeterminate = false
        groupX.indeterminate = false
        otherR.indeterminate = false
        otherW.indeterminate = false
        otherX.indeterminate = false

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

    def computeFinalMode(old: String): String = {
      def bit(cbR: CheckBox, cbW: CheckBox, cbX: CheckBox, base: String): String = {
        val r = if (cbR.indeterminate()) base.charAt(0) else if (cbR.selected()) 'r' else '-'
        val w = if (cbW.indeterminate()) base.charAt(1) else if (cbW.selected()) 'w' else '-'
        val x = if (cbX.indeterminate()) base.charAt(2) else if (cbX.selected()) 'x' else '-'
        s"$r$w$x"
      }

      bit(ownerR, ownerW, ownerX, old.substring(0, 3)) +
        bit(groupR, groupW, groupX, old.substring(3, 6)) +
        bit(otherR, otherW, otherX, old.substring(6, 9))
    }

    def showError(msg: String): Unit = {
      val d = new ThemedDialog[Unit] {
        title = "Error"
        headerText = "Wrong input"
        dialogPane.buttonTypes = Seq(ButtonType.OK)
        dialogPane.content = new VBox {
          children = Seq(new Label(msg))
        }
      }
      d.showAndWaitDialog()
    }

    def validateOwner(n: String): Boolean = if (n.trim.isEmpty) true else Seq("id", "-u", n.trim).! == 0

    def validateGroup(n: String): Boolean = if (n.trim.isEmpty) true else Seq("getent", "group", n.trim).! == 0

    val ownerBox = new HBox(5, ownerR, ownerW, ownerX)
    val groupBox = new HBox(5, groupR, groupW, groupX)
    val otherBox = new HBox(5, otherR, otherW, otherX)

    val permGrid = new GridPane
    permGrid.hgap = 10
    permGrid.vgap = 6
    permGrid.add(new Label("Owner"), 0, 0)
    permGrid.add(ownerBox, 1, 0)
    permGrid.add(new Label("Group"), 0, 1)
    permGrid.add(groupBox, 1, 1)
    permGrid.add(new Label("Other"), 0, 2)
    permGrid.add(otherBox, 1, 2)

    val ogGrid = new GridPane
    ogGrid.hgap = 10
    ogGrid.vgap = 6
    ogGrid.add(new Label("Owner"), 0, 0)
    ogGrid.add(ownerField, 1, 0)
    ogGrid.add(new Label("Group"), 0, 1)
    ogGrid.add(groupField, 1, 1)

    val box = new VBox(10,
      new Label("Rights"),
      permGrid,
      new Label("Octal"),
      octal,
      new Label("Owner and Groups"),
      ogGrid,
      recursive,
      apply
    )
    box.padding = Insets(10)

    val dlg = new ThemedDialog[Unit] {
      title = "Properties"
      headerText = label
      dialogPane.buttonTypes = Seq(ButtonType.Close)
      dialogPane.content = box
    }

    apply.onAction = _ => {
      if (octal.text.value.nonEmpty && !octal.text.value.matches("[0-7]{3}")) {
        showError("Octal-Value is wrong")
        return
      }

      val ownerName = ownerField.text.value.trim
      val groupName = groupField.text.value.trim

      if (!validateOwner(ownerName)) {
        showError("Owner does not exist.")
        return
      }

      if (!validateGroup(groupName)) {
        showError("Group does not exist.")
        return
      }

      new Thread(() => {
        files.foreach { f =>
          val current = readPermissions(f)
          val newPerm = computeFinalMode(current)
          val newOct = rwxToOctal(newPerm)

          if (recursive.selected())
            Seq("pkexec", "chmod", "-R", newOct, f).!
          else
            Seq("pkexec", "chmod", newOct, f).!

          if (ownerName.nonEmpty || groupName.nonEmpty) {
            val spec =
              if (ownerName.nonEmpty && groupName.nonEmpty) s"$ownerName:$groupName"
              else if (ownerName.nonEmpty) ownerName
              else ":" + groupName

            if (recursive.selected())
              Seq("pkexec", "chown", "-R", spec, f).!
            else
              Seq("pkexec", "chown", spec, f).!
          }
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

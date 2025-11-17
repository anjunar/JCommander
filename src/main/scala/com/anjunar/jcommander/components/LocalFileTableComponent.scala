package com.anjunar.jcommander.components

import com.anjunar.jcommander.utils.CdiUtils.*
import com.anjunar.jcommander.commands.*
import com.anjunar.jcommander.configuration.FileTableConf
import com.anjunar.jcommander.files.{FileItem, FileUtils, FileWatcher}
import com.anjunar.jcommander.manager.{FileManager, FileTableManager}
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import javafx.scene.control.TableRow
import javafx.scene.control.skin.{TableViewSkin, VirtualFlow}
import javafx.scene.input.KeyCode
import org.apache.commons.vfs2.{FileObject, FileSystemManager}
import scalafx.Includes.*
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.TableColumn.SortType
import scalafx.scene.control.TableView
import scalafx.scene.input.MouseButton

import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.{Files, Path, WatchEvent}
import java.text.SimpleDateFormat

class LocalFileTableComponent(manager : FileSystemManager) extends AbstractFileTableComponent(manager) {

  val fileTableManager: FileTableManager = inject(classOf[FileTableManager])
  val fileUtils: FileManager = inject(classOf[FileManager])
  
  var currentWatcher: Option[FileWatcher] = None

  override def resolveDirectory: FileObject = manager.toFileObject(Path.of(directory))

  override def processNode(node: TableView[FileItem]): Unit = {
    node.onMouseClicked = e => {
      if (e.button == MouseButton.Secondary) {
        fileUtils.fileContext(this)
        e.consume()
      } else if (e.clickCount == 2) {
        onFileEnter()
        e.consume()
      }
    }

    node.onKeyPressed = e => {
      e.getCode match {
        case KeyCode.ENTER =>
          onFileEnter()
          e.consume()
        case KeyCode.F2 => inject(classOf[RenameCommand]).execute()
        case KeyCode.F3 => inject(classOf[EditCommand]).execute()
        case KeyCode.F4 => inject(classOf[ConsoleCommand]).execute()
        case KeyCode.F5 => inject(classOf[CopyCommand]).execute()
        case KeyCode.F6 => inject(classOf[MoveCommand]).execute()
        case KeyCode.F7 => inject(classOf[MkDirCommand]).execute()
        case KeyCode.F8 => inject(classOf[DeleteCommand]).execute()
        case KeyCode.F10 => inject(classOf[QuitCommand]).execute()
        case _ =>
      }
    }
  }

  def onFileEnter(): Unit = {
    val selected = node.selectionModel().getSelectedItem
    if (selected != null) {
      if (selected.isDir) {
        loadDirectory(selected.file)
      } else {
        fileUtils.executeFile(this)
      }
    }
  }

  def updateFile(file: Path, kind: WatchEvent.Kind[?]): Unit = {
    val itemOpt = node.items.value.find(item => Path.of(item.file) == file)
    kind match {
      case ENTRY_CREATE =>
        if (itemOpt.isEmpty) node.items.value += createFileItem(file.toFile)
      case ENTRY_DELETE =>
        itemOpt.foreach(node.items.value.remove)
      case ENTRY_MODIFY =>
        itemOpt.foreach { old =>
          val buffer = node.items.value
          val index = buffer.indexOf(old)
          if (index >= 0) buffer.update(index, createFileItem(file.toFile))
        }
    }
  }

  def getFileIcon(item: FileItem): BufferedImage = fileUtils.getFileIcon(item.file, large = false)

  private def createFileItem(file: File): FileItem = {
    val ext = if (file.isDirectory) "<DIR>" else fileExtension(file)
    val size = formatSize(file)
    val date = dateFormat.format(file.lastModified())
    FileItem(file.getName, ext, size, file.length(), date, file.lastModified(), file.getAbsolutePath, file.isDirectory, file.getParent)
  }

  def loadDirectory(value: String): Unit = {
    val dir = if (value.startsWith("file:///" )) {
      File(value.replaceFirst("file:///", ""))
    } else {
      File(value)
    }

    directory = dir.getAbsolutePath
    
    if (fileTableManager.left == this) {
      val configuration = inject(classOf[FileTableConf.Left])
      configuration.file = dir  
    }

    if (fileTableManager.right == this) {
      val configuration = inject(classOf[FileTableConf.Right])
      configuration.file = dir
    }

    val files = Option(dir.listFiles()).getOrElse(Array.empty[File])
    val parent = Option(dir.getParentFile).map(p => FileItem("..", "<UP-DIR>", "<UP-DIR>", 0,  "", 0, p.getAbsolutePath, true, p.getParent)).toSeq
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")

    val fileItems = files.toSeq
      .filter(f => !(f.isDirectory && !Files.isReadable(f.toPath)))
      .map { f =>
        val name = f.getName
        val ext = if (f.isDirectory) "<DIR>" else fileExtension(f)
        val size = if (f.isDirectory) "<DIR>" else formatSize(f)
        val date = sdf.format(f.lastModified())
        FileItem(name, ext, size, f.length(), date, f.lastModified(), f.getAbsolutePath, f.isDirectory, f.getParent)
      }

    // Watcher nur neu starten, wenn Pfad sich geändert hat
    if (currentWatcher.isEmpty || directory != currentWatcher.get.path.toAbsolutePath.toString) {
      currentWatcher.foreach(_.stop())
      val watcher = new FileWatcher(dir.toPath, this)
      watcher.start()
      currentWatcher = Some(watcher)
    }

    node.sortOrder.clear()
    node.sortOrder += node.columns.find(_.text.value == "Name").get
    node.columns.find(_.text.value == "Name").get.sortType = SortType.Ascending
    node.sort() // ← Einmalig, sicher!

    val buffer = ObservableBuffer.from(parent ++ fileItems)
    node.items = buffer

    lastSelections.get(dir.getAbsolutePath).foreach { lastName =>
      buffer.indexWhere(_.name == lastName) match {
        case -1 => ()
        case index =>
          val item = buffer(index)
          node.selectionModel().select(item)

          Platform.runLater { () =>
            val jTable = node.delegate
            jTable.scrollTo(math.max(0, index - 5))

            if (index >= 0) Platform.runLater(() => {
              val skin = jTable.getSkin.asInstanceOf[TableViewSkin[FileItem]]
              val flow = skin.getChildren.get(1).asInstanceOf[VirtualFlow[TableRow[FileItem]]]
              val visibleCount = flow.getLastVisibleCell.getIndex - flow.getFirstVisibleCell.getIndex
              val targetIndex = math.max(0, index - visibleCount / 2)
              jTable.scrollTo(targetIndex)
            })
          }
      }
    }

    onDirectoryChanged.foreach(f => Platform.runLater(() => f(this.directory)))
  }

  def formatSize(file: File): String =
    if (file.isDirectory) "<DIR>"
    else {
      val s = file.length()
      if (s >= 1e9) f"${s / 1e9}%.1f GB"
      else if (s >= 1e6) f"${s / 1e6}%.1f MB"
      else if (s >= 1e3) f"${s / 1e3}%.1f KB"
      else s"$s B"
    }

  def fileExtension(file: File): String = {
    val name = file.getName
    val idx = name.lastIndexOf('.')
    if (idx > 0 && idx < name.length - 1) name.substring(idx + 1).toLowerCase
    else ""
  }

}
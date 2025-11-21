package com.anjunar.jcommander.dsl


import com.anjunar.javafx.dsl.{NodeBuilder, Producer}
import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.traits.HasNode
import com.anjunar.jcommander.files.{FileItem, FileWatcher2}
import com.anjunar.jcommander.manager.FileManager
import com.anjunar.jcommander.utils.CdiUtils.inject
import javafx.scene.control.TableCell
import javafx.embed.swing.SwingFXUtils
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode

import java.io.File
import java.nio.file.{Files, Path, WatchEvent}
import java.text.SimpleDateFormat
import java.nio.file.StandardWatchEventKinds.*
import scala.compiletime.uninitialized

class LocalFileTable extends NodeBuilder[TableView[FileItem]] {

  var directory : String = uninitialized

  private var currentWatcher: Option[FileWatcher2] = None

  private val fileManager : FileManager = inject(classOf[FileManager])

  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm")

  def updateFile(file: Path, kind: WatchEvent.Kind[?]): Unit = {
    val itemOpt = node.getItems.asScala.find(item => Path.of(item.file) == file)
    kind match {
      case ENTRY_CREATE =>
        if (itemOpt.isEmpty) node.getItems.add(createFileItem(file.toFile))
      case ENTRY_DELETE =>
        itemOpt.foreach(node.getColumns.remove)
      case ENTRY_MODIFY =>
        itemOpt.foreach { old =>
          val buffer = node.getItems
          val index = buffer.indexOf(old)
          if (index >= 0) buffer.set(index, createFileItem(file.toFile))
        }
    }
  }

  def loadDirectory(value: String): Unit = {
    val clean = normalize(value)
    val dir = File(clean)
    directory = dir.getAbsolutePath

    val files = Option(dir.listFiles()).getOrElse(Array.empty[File])
    val parent = Option(dir.getParentFile).map(p => FileItem("..", "<UP-DIR>", "<UP-DIR>", 0, "", 0, p.getAbsolutePath, true, p.getParent)).toSeq

    val fileItems = files.toSeq
      .filter(f => !(f.isDirectory && !Files.isReadable(f.toPath)))
      .map { f => createFileItem(f)}

    if (currentWatcher.isEmpty || directory != currentWatcher.get.path.toAbsolutePath.toString) {
      currentWatcher.foreach(_.stop())
      val watcher = new FileWatcher2(dir.toPath, this)
      watcher.start()
      currentWatcher = Some(watcher)
    }

    node.getSortOrder.clear()
    node.getSortOrder.add(node.getColumns.asScala.find(_.getText == "Name").get)
    node.sort()
    node.getItems.clear()
    node.getItems.addAll((parent ++ fileItems)*)
  }

  private def createFileItem(file: File): FileItem = {
    val ext = if (file.isDirectory) "<DIR>" else fileExtension(file)
    val size = formatSize(file)
    val date = dateFormat.format(file.lastModified())
    FileItem(file.getName, ext, size, file.length(), date, file.lastModified(), file.getAbsolutePath, file.isDirectory, file.getParent)
  }

  private def formatSize(file: File): String =
    if (file.isDirectory) "<DIR>"
    else {
      val s = file.length()
      if (s >= 1e9) f"${s / 1e9}%.1f GB"
      else if (s >= 1e6) f"${s / 1e6}%.1f MB"
      else if (s >= 1e3) f"${s / 1e3}%.1f KB"
      else s"$s B"
    }

  private def fileExtension(file: File): String = {
    val name = file.getName
    val idx = name.lastIndexOf('.')
    if (idx > 0 && idx < name.length - 1) name.substring(idx + 1).toLowerCase
    else ""
  }

  private def normalize(path: String): String =
    if (path.startsWith("file:")) path.stripPrefix("file:") else path

  override val node: TableView[FileItem] = component[TableView[FileItem]] {
    tableView[FileItem]() {

      HasNode.onKeyPressed = event => {
        if (event.getCode == KeyCode.ENTER) {
          loadDirectory(node.getSelectionModel.getSelectedItem.file)
        }
      }

      tableColumn[FileItem, FileItem]() {
        text = "Name"

        cellValueFactory = (item : FileItem) => item
        cellFactory = (item: FileItem, empty: Boolean, tableCell: TableCell[FileItem, FileItem]) => {
          if (empty) {
            tableCell.setText(null)
            tableCell.setGraphic(null)
          } else {
            tableCell.setText(item.name)
            tableCell.setGraphic(component {
              ImageView() {
                fitWidth = 18
                fitHeight = 18
                image = SwingFXUtils.toFXImage(fileManager.getFileIcon(item.file, false), null)
              }
            })
          }
        }
      }

      tableColumn[FileItem, String]() {
        text = "Extension"
        prefWidth = 100
        cellValueFactory = (fileItem : FileItem) => fileItem.ext
      }

      tableColumn[FileItem, String]() {
        text = "Size"
        prefWidth = 100
        cellValueFactory = (fileItem: FileItem) => fileItem.size
      }

      tableColumn[FileItem, String]() {
        text = "Modified Date"
        prefWidth = 100
        cellValueFactory = (fileItem: FileItem) => fileItem.date
      }


    }
  }

  override def build(): TableView[FileItem] = node

}

object LocalFileTable extends Producer[LocalFileTable, TableView[FileItem]] {

  override def createBuilder: LocalFileTable = new LocalFileTable()

  object HastLocalFileTable {

    def directory()(using l: LocalFileTable) : String = l.directory
    def directory_=(value : String)(using l: LocalFileTable) : Unit = l.loadDirectory(value)

  }

}

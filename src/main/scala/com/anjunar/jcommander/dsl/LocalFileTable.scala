package com.anjunar.jcommander.dsl


import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.{NodeBuilder, Producer, Ref}
import com.anjunar.javafx.dsl.traits.HasEventHandler.addEventHandler
import com.anjunar.jcommander.dsl.AbstractFileTable.loadImages
import com.anjunar.jcommander.files.{FileItem, FileWatcher2}
import com.anjunar.jcommander.manager.FileManager
import com.anjunar.jcommander.utils.CdiUtils.inject
import com.anjunar.jcommander.utils.FileSystemManagerBuilder
import javafx.event.EventHandler
import javafx.scene.control.TableView
import javafx.scene.input.{KeyCode, KeyEvent, MouseButton, MouseEvent}
import org.apache.commons.vfs2.{FileObject, FileSystemManager}

import java.io.File
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.{Files, Path, WatchEvent}
import java.text.SimpleDateFormat
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.beans.value.{ChangeListener, ObservableValue}

import java.lang
class LocalFileTable extends NodeBuilder[TableView[FileItem]], FileTable {

  var directory: String = uninitialized

  override val manager: FileSystemManager = FileSystemManagerBuilder.build()

  override def lastSelections: mutable.Map[String, String] = abstractFileTableRef.value.lastSelections

  private var currentWatcher: Option[FileWatcher2] = None

  private val fileManager: FileManager = inject(classOf[FileManager])

  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  
  private val abstractFileTableRef = Ref[AbstractFileTable]()

  override def resolveDirectory: FileObject =
    manager.toFileObject(Path.of(normalize(directory)))


  def loadDirectory(value: String): Unit = {
    val clean = normalize(value)
    val dir = File(clean)
    directory = dir.getAbsolutePath

    val files = Option(dir.listFiles()).getOrElse(Array.empty[File])
    val parent = Option(dir.getParentFile).map(p => FileItem("..", "<UP-DIR>", "<UP-DIR>", 0, "", 0, p.getAbsolutePath, true, p.getParent)).toSeq

    val fileItems = files.toSeq
      .filter(f => !(f.isDirectory && !Files.isReadable(f.toPath)))
      .map { f => createFileItem(f) }

    if (currentWatcher.isEmpty || directory != currentWatcher.get.path.toAbsolutePath.toString) {
      currentWatcher.foreach(_.stop())
      val watcher = new FileWatcher2(dir.toPath, this)
      watcher.start()
      currentWatcher = Some(watcher)
    }

    node.getItems.clear()
    node.getItems.addAll((parent ++ fileItems) *)

    node.getSortOrder.clear()
    val option = node.getColumns.asScala.find(_.getText == "Name")
    if (option.nonEmpty) {
      node.getSortOrder.add(option.get)
      node.sort()
    }
  }

  def updateFile(file: Path, kind: WatchEvent.Kind[?]): Unit = {
    val itemOpt = node.getItems.asScala.find(item => Path.of(item.file) == file)
    kind match {
      case ENTRY_CREATE =>
        if (itemOpt.isEmpty) node.getItems.add(createFileItem(file.toFile))
      case ENTRY_DELETE =>
        itemOpt.foreach(item => node.getItems.remove(item))
      case ENTRY_MODIFY =>
        itemOpt.foreach { old =>
          val buffer = node.getItems
          val index = buffer.indexOf(old)
          if (index >= 0) buffer.set(index, createFileItem(file.toFile))
        }
    }
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

  lazy val node : TableView[FileItem] = {
    val localFileTable = component[TableView[FileItem]] {
      AbstractFileTable(abstractFileTableRef) {
        loadImages = true
        addEventHandler(KeyEvent.KEY_PRESSED, { event => {
          if (event.getCode == KeyCode.ENTER) {
            val selectedItem = node.getSelectionModel.getSelectedItem
            if (selectedItem.isDir || selectedItem.isUpDir) {
              loadDirectory(selectedItem.file)
            } else {
              fileManager.executeFile(this)
            }
          }
        }
        })
        addEventHandler(MouseEvent.MOUSE_CLICKED, { (event: MouseEvent) => {
          if (event.getButton == MouseButton.SECONDARY) {
            fileManager.fileContext(this, event)
          } else if (event.getClickCount == 2) {
            val selectedItem = node.getSelectionModel.getSelectedItem
            if (selectedItem.isDir || selectedItem.isUpDir) {
              loadDirectory(selectedItem.file)
            } else {
              fileManager.executeFile(this)
            }
          }
        }
        })
      }
    }

    localFileTable
  }

  override def build(): TableView[FileItem] = node

}

object LocalFileTable extends Producer[LocalFileTable, TableView[FileItem]] {

  override def createBuilder: LocalFileTable = new LocalFileTable()

  def directory()(using l: LocalFileTable): String = l.directory

  def directory_=(value: String)(using l: LocalFileTable): Unit = l.loadDirectory(value)

}

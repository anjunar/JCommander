package com.anjunar.jcommander.components

import com.anjunar.jcommander.commands.*
import com.anjunar.jcommander.files.FileItem
import com.anjunar.jcommander.utils.CdiUtils.inject
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import javafx.scene.control.TableRow
import javafx.scene.control.skin.{TableViewSkin, VirtualFlow}
import javafx.scene.input.KeyCode
import org.apache.commons.vfs2.*
import scalafx.Includes.*
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.TableColumn.SortType
import scalafx.scene.control.TableView

import java.awt.image.BufferedImage
import scala.collection.immutable.Seq

class VFS2FileTableComponent(manager : FileSystemManager) extends AbstractFileTableComponent(manager) {

  override def resolveDirectory: FileObject = manager.resolveFile(directory)

  override def processNode(node: TableView[FileItem]): Unit = {
    node.onMouseClicked = e => {
      if (e.clickCount == 2) {
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
      }
    }
  }

  override def getFileIcon(item: FileItem): BufferedImage = null

  override def loadDirectory(directory: String): Unit = {
    val folder: FileObject = manager.resolveFile(directory)
    if (folder == null || !folder.exists() || !folder.isFolder) {
      node.items.value.clear()
      return
    }

    this.directory = folder.getName.getURI

    val children = folder.getChildren.toSeq

    val items = if (folder.getParent == null) then
      children.map { createFileItem(_) }
    else
      Seq(createFileItem(folder.getParent, true)) ++ children.map { createFileItem(_) }

    node.items.value.clear()
    node.items.value.addAll(items*)

    node.sortOrder.clear()
    node.sortOrder += node.columns.find(_.text.value == "Name").get
    node.columns.find(_.text.value == "Name").get.sortType = SortType.Ascending
    node.sort()

    val buffer = ObservableBuffer.from(items)
    node.items = buffer

    lastSelections.get(this.directory).foreach { lastName =>
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

  private def createFileItem(entry: FileObject, upDir: Boolean = false) = {
    val name = entry.getName.getBaseName
    val isDir = entry.getType == FileType.FOLDER

    val size =
      if (isDir) "<DIR>"
      else formatSize(entry.getContent.getSize)

    val lastModifiedTime = try {
      entry.getContent.getLastModifiedTime
    } catch {
      case ex : Exception => System.currentTimeMillis()
    }

    val date = formatDate(lastModifiedTime)

    FileItem(
      name = if upDir then ".." else name,
      ext = getExtension(name),
      size = size,
      sizeLong = if isDir then 0 else entry.getContent.getSize,
      date = date,
      dateLong = lastModifiedTime,
      file = entry.getName.getURI,
      isDir = isDir,
      parent = if (entry.getParent == null) then null else entry.getParent.getName.getURI
    )
  }

  def getExtension(name: String): String =
    name.lastIndexOf('.') match {
      case -1 => ""
      case i => name.substring(i + 1).toLowerCase
    }

  def formatSize(bytes: Long): String =
    if (bytes < 1024) s"${bytes} B"
    else {
      val z = (63 - java.lang.Long.numberOfLeadingZeros(bytes)) / 10
      f"${bytes.toDouble / (1L << (z * 10))}%.1f ${" KMGTPE"(z)}B"
    }

  def formatDate(epochMillis: Long): String =
    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
      .format(java.time.Instant.ofEpochMilli(epochMillis)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDateTime)
}

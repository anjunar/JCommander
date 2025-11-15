package com.anjunar.jcommander.components

import com.anjunar.jcommander.CdiUtils.inject
import com.anjunar.jcommander.commands.*
import com.anjunar.jcommander.files.FileItem
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import javafx.scene.input.KeyCode
import org.apache.commons.vfs2.*
import scalafx.Includes.jfxMouseEvent2sfx
import scalafx.scene.control.TableView

import java.awt.image.BufferedImage

class SFTPFileTableComponent(manager : FileSystemManager) extends AbstractFileTableComponent {

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

    val children = folder.getChildren.toSeq

    val items = children.map { entry =>
      val name = entry.getName.getBaseName
      val isDir = entry.getType == FileType.FOLDER

      val size =
        if (isDir) "<DIR>"
        else formatSize(entry.getContent.getSize)

      val date = formatDate(entry.getContent.getLastModifiedTime)

      FileItem(
        name = name,
        ext = getExtension(name),
        size = size,
        date = date,
        file = entry.getName.getURI,
        isDir = isDir
      )
    }

    node.items.value.clear()
    node.items.value.addAll(items*)
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

package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.*
import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.traits.HasEventHandler.addEventHandler
import com.anjunar.jcommander.components.VFS2ClientComponent.Connection
import com.anjunar.jcommander.dsl.AbstractFileTable.loadImages
import com.anjunar.jcommander.dsl.traits.HasDirectory
import com.anjunar.jcommander.files.FileItem
import javafx.scene.control.TableView
import javafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import org.apache.commons.vfs2.{FileObject, FileSystemManager, FileType}
import scalafx.scene.control.TableColumn.SortType

import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.beans.property.SimpleStringProperty

class VFS2FileTable(connection: Connection) extends NodeBuilder[TableView[FileItem]], FileTable, HasDirectory {

  val manager: FileSystemManager = connection.manager

  var directoryProperty = new SimpleStringProperty("")

  override def lastSelections: mutable.Map[String, String] = abstractFileTableRef.value.lastSelections

  override def resolveDirectory: FileObject = manager.resolveFile(directoryProperty.get())

  private val abstractFileTableRef = Ref[AbstractFileTable]()

  override def loadDirectory(directory: String): Unit = {
    val folder: FileObject = manager.resolveFile(directory)
    if (folder == null || !folder.exists() || !folder.isFolder) {
      node.getItems.clear()
      return
    }

    directoryProperty.set(folder.getName.getURI)

    val children = folder.getChildren.toSeq

    val items = if (folder.getParent == null) then
      children.map {
        createFileItem(_)
      }
    else
      Seq(createFileItem(folder.getParent, true)) ++ children.map {
        createFileItem(_)
      }

    node.getItems.clear()
    node.getItems.addAll(items *)

    node.getSortOrder.clear()
    node.getSortOrder.add(node.getColumns.asScala.find(_.getText == "Name").get)
    node.getColumns.asScala.find(_.getText == "Name").get.setSortType(SortType.ASCENDING)
    node.sort()
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
      case ex: Exception => System.currentTimeMillis()
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

  lazy val node : TableView[FileItem] = {
    val vsf2FileTable = component[TableView[FileItem]] {
      AbstractFileTable(abstractFileTableRef) {
        loadImages = false
        addEventHandler(KeyEvent.KEY_PRESSED, { event => {
          if (event.getCode == KeyCode.ENTER) {
            val selectedItem = node.getSelectionModel.getSelectedItem
            if (selectedItem.isDir || selectedItem.isUpDir) {
              loadDirectory(selectedItem.file)
            }
          }
        }})
        addEventHandler(MouseEvent.MOUSE_CLICKED, { (event: MouseEvent) => {
          if (event.getClickCount == 2) {
            val selectedItem = node.getSelectionModel.getSelectedItem
            if (selectedItem.isDir || selectedItem.isUpDir) {
              loadDirectory(selectedItem.file)
            }
          }
        }})
      }
    }

    vsf2FileTable
  }

  override def afterBuild(): Unit = loadDirectory(directoryProperty.get())

  override def build(): TableView[FileItem] = node

}

object VFS2FileTable {

  def apply(manager: Connection, ref: Ref[VFS2FileTable] = Ref())(body: (VFS2FileTable, BuildContext) ?=> Unit)
           (using ctx: BuildContext, parent: ElementBuilder[?]): TableView[FileItem] =
    DSL.create[TableView[FileItem], VFS2FileTable](ref, new VFS2FileTable(manager))(body)

  def build(manager: Connection, ref: Ref[VFS2FileTable] = Ref[VFS2FileTable]())(body: (VFS2FileTable, BuildContext) ?=> Unit): VFS2FileTable = {
    DSL.createBuilder[TableView[FileItem], VFS2FileTable](ref, new VFS2FileTable(manager))(body)(using ctx = new BuildContext)
  }


}

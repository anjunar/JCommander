package com.anjunar.jcommander.components

import com.anjunar.jcommander.files.FileItem
import javafx.scene.control.TableCell as JfxTableCell
import org.apache.commons.vfs2.FileSystemManager
import scalafx.Includes.*
import scalafx.beans.property.ReadOnlyObjectWrapper
import scalafx.embed.swing.SwingFXUtils
import scalafx.scene.control.{TableCell, TableColumn, TableView}
import scalafx.scene.image.ImageView

import java.awt.image.BufferedImage
import java.text.SimpleDateFormat
import java.util.Comparator
import scala.collection.mutable
import scala.compiletime.uninitialized

abstract class AbstractFileTableComponent(val manager : FileSystemManager) extends Component[TableView[FileItem]] {

  val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  val lastSelections = mutable.Map[String, String]()

  var directory: String = uninitialized

  val activeTable = new TableView[FileItem]()

  def processNode(node: TableView[FileItem]): Unit

  val node: TableView[FileItem] = {
    val newNode = new TableView[FileItem] {
      selectionModel().selectionMode = scalafx.scene.control.SelectionMode.Multiple

      val nameCol = new TableColumn[FileItem, FileItem] {
        text = "Name"
        cellValueFactory = f => ReadOnlyObjectWrapper(f.value)

        cellFactory = { (_: TableColumn[FileItem, FileItem]) =>
          new TableCell[FileItem, FileItem] {
            override val delegate: JfxTableCell[FileItem, FileItem] =
              new JfxTableCell[FileItem, FileItem]() {
                private val imageView = new ImageView {
                  fitWidth = 18
                  fitHeight = 18
                  preserveRatio = true
                }

                override def updateItem(item: FileItem, empty: Boolean): Unit = {
                  super.updateItem(item, empty)
                  if (empty || item == null) {
                    setText(null)
                    setGraphic(null)
                  } else {
                    setText(item.name)
                    val icon = item.icon.value
                    val fxImg =
                      if (icon != null) {
                        icon
                      }
                      else {
                        val img = getFileIcon(item)
                        item.icon.value = img
                        img
                      }
                    if (fxImg != null) {
                      imageView.image = SwingFXUtils.toFXImage(fxImg, null)
                      setGraphic(imageView)
                    }
                  }
                }
              }
          }
        }
      }

      val extCol = new TableColumn[FileItem, FileItem] {
        text = "Extension"
        prefWidth = 100
        resizable = false

        cellValueFactory = f => ReadOnlyObjectWrapper(f.value)

        cellFactory = { (_: TableColumn[FileItem, FileItem]) =>
          new TableCell[FileItem, FileItem] {
            override val delegate: JfxTableCell[FileItem, FileItem] =
              new JfxTableCell[FileItem, FileItem]() {
                override def updateItem(item: FileItem, empty: Boolean): Unit = {
                  super.updateItem(item, empty)
                  if (empty || item == null) setText(null)
                  else setText(if (item.isDir) "<DIR>" else item.ext)
                }
              }
          }
        }
      }

      val sizeCol = new TableColumn[FileItem, FileItem] {
        text = "Size"
        prefWidth = 100
        resizable = false
        style = "-fx-alignment: CENTER-RIGHT;"

        cellValueFactory = f => ReadOnlyObjectWrapper(f.value)

        cellFactory = { (_: TableColumn[FileItem, FileItem]) =>
          new TableCell[FileItem, FileItem] {
            override val delegate: JfxTableCell[FileItem, FileItem] =
              new JfxTableCell[FileItem, FileItem]() {
                override def updateItem(item: FileItem, empty: Boolean): Unit = {
                  super.updateItem(item, empty)
                  if (empty || item == null) setText(null)
                  else setText(item.size)
                }
              }
          }
        }
      }

      val dateCol = new TableColumn[FileItem, FileItem] {
        text = "Changed"
        prefWidth = 160
        resizable = false

        cellValueFactory = f => ReadOnlyObjectWrapper(f.value)

        cellFactory = { (_: TableColumn[FileItem, FileItem]) =>
          new TableCell[FileItem, FileItem] {
            override val delegate: JfxTableCell[FileItem, FileItem] =
              new JfxTableCell[FileItem, FileItem]() {
                override def updateItem(item: FileItem, empty: Boolean): Unit = {
                  super.updateItem(item, empty)
                  if (empty || item == null) setText(null)
                  else setText(item.date)
                }
              }
          }
        }
      }

      columns ++= Seq(nameCol, extCol, sizeCol, dateCol)
      columns.foreach(_.setReorderable(false))

      width.onChange { (_, _, newWidth) =>
        val totalFixed = extCol.width.value + sizeCol.width.value + dateCol.width.value + 2
        val newPref = newWidth.doubleValue() - totalFixed
        if (newPref > 100) nameCol.prefWidth = newPref
      }

      selectionModel().selectedItemProperty().onChange { (_, _, newItem) =>
        if (newItem != null && newItem.file != null && newItem.parent != null) {
          lastSelections.update(newItem.parent, newItem.name)
        }
      }

      delegate.setSortPolicy(tv => {
        val sortOrder = tv.getSortOrder
        val sortCol = if (sortOrder.isEmpty) None else Some(sortOrder.get(0))
        val sortType = sortCol.map(_.getSortType).getOrElse(javafx.scene.control.TableColumn.SortType.ASCENDING)

        val upDirFirst: Comparator[FileItem] = (a, b) =>
          if (a.isUpDir && !b.isUpDir) -1
          else if (!a.isUpDir && b.isUpDir) 1
          else 0

        val dirFirst: Comparator[FileItem] = (a, b) =>
          if (a.isDir && !b.isDir && !a.isUpDir && !b.isUpDir) -1
          else if (!a.isDir && b.isDir && !a.isUpDir && !b.isUpDir) 1
          else 0

        val secondary: Comparator[FileItem] = sortCol match {
          case Some(col) => col.getText match {
            case "Name" =>
              (a, b) => {
                val r = a.name.compareToIgnoreCase(b.name)
                if (sortType == javafx.scene.control.TableColumn.SortType.ASCENDING) r else -r
              }
            case "Extension" =>
              (a, b) => {
                val ea = if (a.isDir || a.isUpDir) "" else a.ext
                val eb = if (b.isDir || b.isUpDir) "" else b.ext
                val r = ea.compareToIgnoreCase(eb)
                if (sortType == javafx.scene.control.TableColumn.SortType.ASCENDING) r else -r
              }
            case "Size" =>
              (a, b) => {
                val sa = if (a.isDir || a.isUpDir) Long.MinValue else a.sizeLong
                val sb = if (b.isDir || b.isUpDir) Long.MinValue else b.sizeLong
                val r = java.lang.Long.compare(sa, sb)
                if (sortType == javafx.scene.control.TableColumn.SortType.ASCENDING) r else -r
              }
            case "Changed" =>
              (a, b) => {
                val r = java.lang.Long.compare(a.dateLong, b.dateLong)
                if (sortType == javafx.scene.control.TableColumn.SortType.ASCENDING) r else -r
              }
            case _ => (_: FileItem, _: FileItem) => 0
          }
          case None =>
            (a, b) => a.name.compareToIgnoreCase(b.name)
        }

        val finalComp = upDirFirst.thenComparing(dirFirst).thenComparing(secondary)
        javafx.collections.FXCollections.sort(tv.getItems, finalComp)
        true
      })

    }

    processNode(newNode)

    newNode
  }

  def getFileIcon(item: FileItem): BufferedImage

  def loadDirectory(directory: String): Unit
  
  override def toString = s"AbstractFileTableComponent($directory)"
}

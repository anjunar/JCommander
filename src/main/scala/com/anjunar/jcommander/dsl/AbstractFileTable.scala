package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.traits.IsNode.addEventHandler
import com.anjunar.javafx.dsl.traits.HasText.text
import com.anjunar.javafx.dsl.traits.HasWidth.{prefWidth, widthProperty}
import com.anjunar.javafx.dsl.{NodeBuilder, Producer}
import com.anjunar.javafx.scene.control.{tableColumn, tableView}
import com.anjunar.javafx.scene.control.tableView.IsTableView.{selectionModel, sortPolicy}
import com.anjunar.javafx.scene.control.tableColumn.IsTableColumn.{cellFactory, cellValueFactory}
import com.anjunar.javafx.scene.image.imageView
import com.anjunar.javafx.scene.image.imageView.IsImageView.{fitHeight, fitWidth, image}
import com.anjunar.jcommander.commands.*
import com.anjunar.jcommander.files.{FileItem, FileWatcher2}
import com.anjunar.jcommander.manager.{FileManager, FileTableManager}
import com.anjunar.jcommander.utils.CdiUtils.inject
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.FXCollections
import javafx.embed.swing.SwingFXUtils
import javafx.scene.Node
import javafx.scene.control.{TableCell, TableColumn, TableView}
import javafx.scene.input.{KeyCode, KeyEvent}

import java.io.File
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.{Files, Path, WatchEvent}
import java.text.SimpleDateFormat
import java.util.Comparator
import scala.collection.mutable
import scala.compiletime.uninitialized

class AbstractFileTable extends NodeBuilder[TableView[FileItem]] {

  private val fileManager: FileManager = inject(classOf[FileManager])

  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm")

  val lastSelections = mutable.Map[String, String]()

  var loadImages : Boolean = false

  lazy val node : TableView[FileItem] = {
    val abstractFileTable = component[TableView[FileItem]] {
      tableView[FileItem]() {

        addEventHandler(KeyEvent.KEY_PRESSED, { event => {
          event.getCode match {
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
        })

        val nameCol = tableColumn[FileItem, FileItem]() {
          text = "Name"

          cellValueFactory = (item: FileItem) => item
          cellFactory = (item: FileItem, empty: Boolean, tableCell: TableCell[FileItem, FileItem]) => {
            if (empty) {
              tableCell.setText(null)
              tableCell.setGraphic(null)
            } else {
              tableCell.setText(item.name)
              tableCell.setGraphic(component {
                imageView() {
                  fitWidth = 18
                  fitHeight = 18
                  image = SwingFXUtils.toFXImage(fileManager.getFileIcon(item.file, false), null)
                }
              })
            }
          }
        }

        val extCol = tableColumn[FileItem, String]() {
          text = "Extension"
          prefWidth = 100
          cellValueFactory = (fileItem: FileItem) => fileItem.ext
        }

        val sizeCol = tableColumn[FileItem, String]() {
          text = "Size"
          prefWidth = 100
          cellValueFactory = (fileItem: FileItem) => fileItem.size
        }

        val dateCol = tableColumn[FileItem, String]() {
          text = "Modified Date"
          prefWidth = 100
          cellValueFactory = (fileItem: FileItem) => fileItem.date
        }


        val tableColumns = Seq(nameCol, extCol, sizeCol, dateCol)

        tableColumns.foreach(column => column.setReorderable(false))

        widthProperty().addListener({ (_, oldValue, newValue) => {
          val totalFixed = extCol.getWidth + sizeCol.getWidth + dateCol.getWidth + 2
          val newPref = newValue.doubleValue() - totalFixed
          if (newPref > 100) nameCol.setPrefWidth(newPref)
        } })

        selectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) => {
          if (newValue != null && newValue.file != null && newValue.parent != null) {
            lastSelections.update(newValue.parent, newValue.file)
          }
        })

        sortPolicy = (tv : TableView[FileItem]) => {
          val sortOrder = tv.getSortOrder
          val sortCol = if (sortOrder.isEmpty) None else Some(sortOrder.get(0))
          val sortType = sortCol.map(_.getSortType).getOrElse(TableColumn.SortType.ASCENDING)

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
                  if (sortType == TableColumn.SortType.ASCENDING) r else -r
                }
              case "Extension" =>
                (a, b) => {
                  val ea = if (a.isDir || a.isUpDir) "" else a.ext
                  val eb = if (b.isDir || b.isUpDir) "" else b.ext
                  val r = ea.compareToIgnoreCase(eb)
                  if (sortType == TableColumn.SortType.ASCENDING) r else -r
                }
              case "Size" =>
                (a, b) => {
                  val sa = if (a.isDir || a.isUpDir) Long.MinValue else a.sizeLong
                  val sb = if (b.isDir || b.isUpDir) Long.MinValue else b.sizeLong
                  val r = java.lang.Long.compare(sa, sb)
                  if (sortType == TableColumn.SortType.ASCENDING) r else -r
                }
              case "Changed" =>
                (a, b) => {
                  val r = java.lang.Long.compare(a.dateLong, b.dateLong)
                  if (sortType == TableColumn.SortType.ASCENDING) r else -r
                }
              case _ => (_: FileItem, _: FileItem) => 0
            }
            case None =>
              (a, b) => a.name.compareToIgnoreCase(b.name)
          }

          val finalComp = upDirFirst.thenComparing(dirFirst).thenComparing(secondary)
          FXCollections.sort(tv.getItems, finalComp)
          true
        }

      }
    }

    abstractFileTable
  }

  override def build(): TableView[FileItem] = node

}

object AbstractFileTable extends Producer[AbstractFileTable, TableView[FileItem]] {

  override def createBuilder: AbstractFileTable = new AbstractFileTable()

  object IsAbstractFileTable {

    def loadImages()(using l: AbstractFileTable): Boolean = l.loadImages

    def loadImages_=(value: Boolean)(using l: AbstractFileTable): Unit = l.loadImages = value

  }

}

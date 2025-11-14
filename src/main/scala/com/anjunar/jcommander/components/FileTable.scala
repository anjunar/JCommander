package com.anjunar.jcommander.components

import com.anjunar.jcommander.files.{FileItem, FileUtils, FileWatcher}
import com.anjunar.jcommander.*
import com.anjunar.jcommander.components.DriveButtons.{OnDriveChangeLeft, OnDriveChangeRight}
import com.anjunar.jcommander.configuration.FileTableConf
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import javafx.scene.control.skin.{TableViewSkin, VirtualFlow}
import javafx.scene.control.{TableRow, TableCell as JfxTableCell}
import scalafx.Includes.*
import scalafx.application.Platform
import scalafx.beans.property.ReadOnlyObjectWrapper
import scalafx.collections.ObservableBuffer
import scalafx.embed.swing.SwingFXUtils
import scalafx.scene.control.TableColumn.SortType
import scalafx.scene.control.{TableCell, TableColumn, TableView}
import scalafx.scene.image.ImageView
import java.nio.file.StandardWatchEventKinds.*

import java.io.File
import java.nio.file.{Files, Path, WatchEvent}
import java.text.SimpleDateFormat
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*

abstract class FileTable extends Component[TableView[FileItem]] {

  val fileUtils: FileUtils = inject(classOf[FileUtils])
  val configuration: FileTableConf

  var directory: File = uninitialized
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  var currentWatcher: Option[FileWatcher] = None
  val lastSelections = mutable.Map[String, String]()

  lazy val node: TableView[FileItem] = new TableView[FileItem] {
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
                    if (icon != null) icon
                    else {
                      val img = fileUtils.getFileIcon(item.file, large = false)
                      item.icon.value = img
                      img
                    }
                  imageView.image = SwingFXUtils.toFXImage(fxImg, null)
                  setGraphic(imageView)
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
                else setText(if (item.file.isDirectory) "<DIR>" else item.ext)
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
      if (newItem != null && newItem.file != null && newItem.file.getParent != null) {
        lastSelections.update(newItem.file.getParent, newItem.name)
      }
    }

    delegate.setSortPolicy(tv => {
      val sortOrder = tv.getSortOrder
      val sortCol = if (sortOrder.isEmpty) None else Some(sortOrder.get(0))
      val sortType = sortCol.map(_.getSortType).getOrElse(javafx.scene.control.TableColumn.SortType.ASCENDING)

      val upDirFirst: java.util.Comparator[FileItem] = (a, b) =>
        if (a.isUpDir && !b.isUpDir) -1
        else if (!a.isUpDir && b.isUpDir) 1
        else 0

      val dirFirst: java.util.Comparator[FileItem] = (a, b) =>
        if (a.file.isDirectory && !b.file.isDirectory && !a.isUpDir && !b.isUpDir) -1
        else if (!a.file.isDirectory && b.file.isDirectory && !a.isUpDir && !b.isUpDir) 1
        else 0

      val secondary: java.util.Comparator[FileItem] = sortCol match {
        case Some(col) => col.getText match {
          case "Name" =>
            (a, b) => {
              val r = a.name.compareToIgnoreCase(b.name)
              if (sortType == javafx.scene.control.TableColumn.SortType.ASCENDING) r else -r
            }
          case "Extension" =>
            (a, b) => {
              val ea = if (a.file.isDirectory || a.isUpDir) "" else a.ext
              val eb = if (b.file.isDirectory || b.isUpDir) "" else b.ext
              val r = ea.compareToIgnoreCase(eb)
              if (sortType == javafx.scene.control.TableColumn.SortType.ASCENDING) r else -r
            }
          case "Size" =>
            (a, b) => {
              val sa = if (a.file.isDirectory || a.isUpDir) Long.MinValue else a.file.length()
              val sb = if (b.file.isDirectory || b.isUpDir) Long.MinValue else b.file.length()
              val r = java.lang.Long.compare(sa, sb)
              if (sortType == javafx.scene.control.TableColumn.SortType.ASCENDING) r else -r
            }
          case "Changed" =>
            (a, b) => {
              val r = java.lang.Long.compare(a.file.lastModified(), b.file.lastModified())
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

  def updateFile(file: Path, kind: WatchEvent.Kind[?]): Unit = {
    val itemOpt = node.items.value.find(_.file.toPath == file)
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

  private def createFileItem(file: File): FileItem = {
    val ext = if (file.isDirectory) "<DIR>" else fileExtension(file)
    val size = formatSize(file)
    val date = dateFormat.format(file.lastModified())
    FileItem(file.getName, ext, size, date, file)
  }

  def loadDirectory(dir: File): Unit = {
    directory = dir
    configuration.file = dir

    val files = Option(dir.listFiles()).getOrElse(Array.empty[File])
    val parent = Option(dir.getParentFile).map(p => FileItem("..", "<UP-DIR>", "<UP-DIR>", "", p, true)).toSeq
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")

    val fileItems = files.toSeq
      .filter(f => !(f.isDirectory && !Files.isReadable(f.toPath)))
      .map { f =>
        val name = f.getName
        val ext = if (f.isDirectory) "<DIR>" else fileExtension(f)
        val size = if (f.isDirectory) "<DIR>" else formatSize(f)
        val date = sdf.format(f.lastModified())
        FileItem(name, ext, size, date, f)
      }

    // Watcher nur neu starten, wenn Pfad sich geändert hat
    if (currentWatcher.isEmpty || directory.toPath != currentWatcher.get.path) {
      currentWatcher.foreach(_.stop())
      val watcher = new FileWatcher(dir.toPath, this)
      watcher.start()
      currentWatcher = Some(watcher)
    }

    val buffer = ObservableBuffer.from(parent ++ fileItems)
    node.items = buffer

    node.sortOrder.clear()
    node.sortOrder += node.columns.find(_.text.value == "Name").get
    node.columns.find(_.text.value == "Name").get.sortType = SortType.Ascending
    node.sort() // ← Einmalig, sicher!

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

object FileTable {

  @ApplicationScoped
  class Left extends FileTable {
    val configuration: FileTableConf = inject(classOf[FileTableConf.Left])

    def onDriveChange(@Observes event: OnDriveChangeLeft): Unit = {
      loadDirectory(event.file)
    }
  }

  @ApplicationScoped
  class Right extends FileTable {
    val configuration: FileTableConf = inject(classOf[FileTableConf.Right])

    def onDriveChange(@Observes event: OnDriveChangeRight): Unit = {
      loadDirectory(event.file)
    }
  }
}

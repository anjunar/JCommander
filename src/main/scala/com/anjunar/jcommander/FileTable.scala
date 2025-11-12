package com.anjunar.jcommander

import com.anjunar.jcommander.files.FileUtils
import jakarta.enterprise.context.ApplicationScoped
import javafx.scene.control.skin.{TableViewSkin, VirtualFlow}
import javafx.scene.control.{TableRow, TableCell as JfxTableCell}
import scalafx.Includes.{jfxObjectProperty2sfx, jfxTableSelectionModel2sfx, observableList2ObservableBuffer}
import scalafx.application.Platform
import scalafx.beans.property.ReadOnlyObjectWrapper
import scalafx.collections.ObservableBuffer
import scalafx.embed.swing.SwingFXUtils
import scalafx.scene.control.{TableCell, TableColumn, TableView}
import scalafx.scene.image.ImageView

import java.io.File
import java.nio.file.Files
import java.text.SimpleDateFormat
import scala.collection.mutable
import scala.compiletime.uninitialized

class FileTable extends Component[TableView[FileItem]] {

  var fileUtils: FileUtils = inject(classOf[FileUtils])

  var directory: File = uninitialized

  val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  var currentWatcher: Option[FileWatcher] = None

  val lastSelections = mutable.Map[String, String]()

  val node: TableView[FileItem] = new TableView[FileItem] {
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
                  if (icon != null) {
                    imageView.image = SwingFXUtils.toFXImage(icon, null)
                    setGraphic(imageView)
                  } else {
                    val iconImg = fileUtils.getFileIcon(item.file, large = false)
                    item.icon.value = iconImg
                    imageView.image = SwingFXUtils.toFXImage(iconImg, null)
                    graphic = imageView
                  }
                }
              }
            }
        }
      }
    }

    val extCol = new TableColumn[FileItem, String] {
      text = "Extension"
      cellValueFactory = f => ReadOnlyObjectWrapper(f.value.ext)
      prefWidth = 100
      resizable = false
    }

    val sizeCol = new TableColumn[FileItem, String] {
      text = "Size"
      cellValueFactory = f => ReadOnlyObjectWrapper(f.value.size)
      prefWidth = 100
      resizable = false
      style = "-fx-alignment: CENTER-RIGHT;"
    }

    val dateCol = new TableColumn[FileItem, String] {
      text = "Changed"
      cellValueFactory = f => ReadOnlyObjectWrapper(f.value.date)
      prefWidth = 160
      resizable = false
    }

    columns ++= Seq(nameCol, extCol, sizeCol, dateCol)

    width.onChange { (_, _, newWidth) =>
      val totalFixed = extCol.width.value + sizeCol.width.value + dateCol.width.value + 2
      val newPref = newWidth.doubleValue() - totalFixed
      if (newPref > 100) nameCol.prefWidth = newPref
    }

    columns.foreach(_.setReorderable(false))

    selectionModel.value.getSelectedItems.onChange { (_, _) =>
      val selected = selectionModel.value.getSelectedItem
      if (selected != null && selected.file != null && selected.file.getParent != null) {
        lastSelections.update(selected.file.getParent, selected.name)
      }
    }
  }

  def loadDirectory(dir: File): Unit = {
    directory = dir
    val files = Option(dir.listFiles()).getOrElse(Array.empty[File])
    val parent = Option(dir.getParentFile).map(p => FileItem("..", "<DIR>", "", "", p)).toSeq
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")

    val fileItems = files.toSeq
      .filter(f => !(f.isDirectory && !Files.isReadable(f.toPath)))
      .sortBy(f => (!f.isDirectory, f.getName.toLowerCase))
      .map { f =>
        val name = f.getName
        val ext = if (f.isDirectory) "<DIR>" else fileExtension(f)
        val size = if (f.isDirectory) "<DIR>" else formatSize(f)
        val date = sdf.format(f.lastModified())
        FileItem(name, ext, size, date, f)
      }

    currentWatcher.foreach(_.stop())
    val watcher = new FileWatcher(dir.toPath, this)
    watcher.start()
    currentWatcher = Some(watcher)

    val buffer = ObservableBuffer.from(parent ++ fileItems)
    node.items = buffer

    lastSelections.get(dir.getAbsolutePath).foreach { lastName =>
      buffer.find(_.name == lastName).foreach { item =>
        node.selectionModel.value.select(item)

        val jTable = node.delegate
        val index = buffer.indexOf(item)
        jTable.scrollTo(index)

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

  def formatSize(file: File): String =
    if (file.isDirectory) "<DIR>"
    else {
      val s = file.length()
      if (s > 1e9) f"${s / 1e9}%.1f GB"
      else if (s > 1e6) f"${s / 1e6}%.1f MB"
      else if (s > 1e3) f"${s / 1e3}%.1f KB"
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
  class Left extends FileTable

  @ApplicationScoped
  class Right extends FileTable

}
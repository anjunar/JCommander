package com.anjunar.jcommander.manager

import com.anjunar.jcommander.components.{AbstractFileTableComponent, LocalFileTableComponent, VFS2FileTableComponent}
import com.anjunar.jcommander.files.{FileUtils, StreamFileUtils, WinFileUtils}
import jakarta.enterprise.context.ApplicationScoped

import java.awt.image.BufferedImage
import scala.jdk.CollectionConverters.*

@ApplicationScoped
class WinFileManager extends FileManager {

  val winFileUtils: FileUtils = new WinFileUtils()
  val streamFiles = new StreamFileUtils()

  override def fileContext(activeTable: AbstractFileTableComponent): Unit = activeTable match {
    case source: LocalFileTableComponent => winFileUtils.fileContext(
      source.node.items.value.asScala.map(_.file).toSeq
    )
  }

  override def getFileIcon(activeTable: AbstractFileTableComponent): BufferedImage = activeTable match {
    case source: LocalFileTableComponent => winFileUtils.getFileIcon(
      source.node.selectionModel.value.getSelectedItem.file,
      false
    )
  }

  override def executeFile(file: String, workingDir : String, args: Seq[String]): Unit = {
    winFileUtils.executeFile(file, workingDir, args)
  }

  override def console(activeTable: AbstractFileTableComponent): Unit = activeTable match {
    case source: LocalFileTableComponent => winFileUtils.console(source.directory)
  }

  override def executeFile(activeTable: AbstractFileTableComponent): Unit = activeTable match {
    case source: LocalFileTableComponent => winFileUtils.executeFile(
      source.node.selectionModel.value.getSelectedItem.file
    )
  }

  override def mkDir(activeTable: AbstractFileTableComponent): Unit = activeTable match {
    case source: LocalFileTableComponent => winFileUtils.mkDir(source)
    case source: VFS2FileTableComponent => streamFiles.mkDir(source)
  }

  override def renameFile(activeTable: AbstractFileTableComponent): Unit = {
    activeTable match {
      case source: LocalFileTableComponent => winFileUtils.renameFile(source)
      case source: VFS2FileTableComponent => streamFiles.renameFile(source)
    }
  }

  override def copyFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit = {
    if (activeTable.isInstanceOf[VFS2FileTableComponent] || otherTable.isInstanceOf[VFS2FileTableComponent]) {
      streamFiles.copyFiles(activeTable, otherTable)
    } else {
      winFileUtils.copyFiles(activeTable, otherTable)
    }
  }

  override def moveFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit = {
    if (activeTable.isInstanceOf[VFS2FileTableComponent] || otherTable.isInstanceOf[VFS2FileTableComponent]) {
      streamFiles.moveFiles(activeTable, otherTable)
    } else {
      winFileUtils.moveFiles(activeTable, otherTable)
    }
  }

  override def deleteFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit = activeTable match {
    case source: LocalFileTableComponent => winFileUtils.deleteFiles(source, otherTable)
    case source: VFS2FileTableComponent => streamFiles.deleteFiles(source, otherTable)
  }

}

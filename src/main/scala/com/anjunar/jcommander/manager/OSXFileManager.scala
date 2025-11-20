package com.anjunar.jcommander.manager

import com.anjunar.jcommander.OSXNativeCopy
import com.anjunar.jcommander.components.{AbstractFileTableComponent, LocalFileTableComponent, VFS2FileTableComponent}
import com.anjunar.jcommander.files.{OSXFileUtils, StreamFileUtils}
import scalafx.scene.input.MouseEvent

import scala.jdk.CollectionConverters.*
import java.awt.image.BufferedImage

class OSXFileManager extends FileManager {

  val fileUtils = new OSXFileUtils()
  val streamFiles = new StreamFileUtils()

  override def fileContext(activeTable: AbstractFileTableComponent, event: MouseEvent): Unit = activeTable match {
    case source: LocalFileTableComponent => fileUtils.fileContext(
      source.node.selectionModel.value.getSelectedItems.asScala.map(_.file).toSeq,
      event
    )
  }

  override def getFileIcon(file: String, large: Boolean): BufferedImage = fileUtils.getFileIcon(file, large)

  override def executeFile(file: String, workingDir: String, args: Seq[String]): Unit = {
    fileUtils.executeFile(file, workingDir, args)
  }

  override def console(activeTable: AbstractFileTableComponent): Unit = activeTable match {
    case source: LocalFileTableComponent => fileUtils.console(source.directory)
  }

  override def executeFile(activeTable: AbstractFileTableComponent): Unit = activeTable match {
    case source: LocalFileTableComponent => fileUtils.executeFile(
      source.node.selectionModel.value.getSelectedItem.file
    )
  }

  override def mkDir(activeTable: AbstractFileTableComponent): Unit = activeTable match {
    case source: LocalFileTableComponent => fileUtils.mkDir(source)
    case source: VFS2FileTableComponent => streamFiles.mkDir(source)
  }

  override def renameFile(activeTable: AbstractFileTableComponent): Unit = {
    activeTable match {
      case source: LocalFileTableComponent => fileUtils.renameFile(source)
      case source: VFS2FileTableComponent => streamFiles.renameFile(source)
    }
  }

  override def copyFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit = {
    if (activeTable.isInstanceOf[VFS2FileTableComponent] || otherTable.isInstanceOf[VFS2FileTableComponent]) {
      streamFiles.copyFiles(activeTable, otherTable)
    } else {
      fileUtils.copyFiles(activeTable, otherTable)
    }
  }

  override def moveFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit = {
    if (activeTable.isInstanceOf[VFS2FileTableComponent] || otherTable.isInstanceOf[VFS2FileTableComponent]) {
      streamFiles.moveFiles(activeTable, otherTable)
    } else {
      fileUtils.moveFiles(activeTable, otherTable)
    }
  }

  override def deleteFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit = activeTable match {
    case source: LocalFileTableComponent => fileUtils.deleteFiles(source, otherTable)
    case source: VFS2FileTableComponent => streamFiles.deleteFiles(source, otherTable)
  }

}

package com.anjunar.jcommander.manager

import com.anjunar.jcommander.dsl.{FileTable, LocalFileTable, VFS2FileTable}
import com.anjunar.jcommander.files.{FileUtils, StreamFileUtils, WinFileUtils}
import javafx.scene.input.MouseEvent

import java.awt.image.BufferedImage
import scala.jdk.CollectionConverters.*

class WinFileManager extends FileManager {

  val fileUtils: FileUtils = new WinFileUtils()
  val streamFiles = new StreamFileUtils()

  override def fileContext(activeTable: FileTable, event: MouseEvent): Unit = activeTable match {
    case source: LocalFileTable => fileUtils.fileContext(source.node.getSelectionModel.getSelectedItems.asScala.map(_.file).toSeq, event)
  }

  override def getFileIcon(file : String, large : Boolean): BufferedImage = fileUtils.getFileIcon(file, large)

  override def executeFile(file: String, workingDir : String, args: Seq[String]): Unit = {
    fileUtils.executeFile(file, workingDir, args)
  }

  override def console(activeTable: FileTable): Unit = activeTable match {
    case source: LocalFileTable => fileUtils.console(source.directory)
  }

  override def executeFile(activeTable: FileTable): Unit = activeTable match {
    case source: LocalFileTable => fileUtils.executeFile(
      source.node.getSelectionModel.getSelectedItem.file
    )
  }

  override def mkDir(activeTable: FileTable): Unit = activeTable match {
    case source: LocalFileTable => fileUtils.mkDir(source)
    case source: VFS2FileTable => streamFiles.mkDir(source)
  }

  override def renameFile(activeTable: FileTable): Unit = {
    activeTable match {
      case source: LocalFileTable => fileUtils.renameFile(source)
      case source: VFS2FileTable => streamFiles.renameFile(source)
    }
  }

  override def copyFiles(activeTable: FileTable, otherTable: FileTable): Unit = {
    if (activeTable.isInstanceOf[VFS2FileTable] || otherTable.isInstanceOf[VFS2FileTable]) {
      streamFiles.copyFiles(activeTable, otherTable)
    } else {
      fileUtils.copyFiles(activeTable, otherTable)
    }
  }

  override def moveFiles(activeTable: FileTable, otherTable: FileTable): Unit = {
    if (activeTable.isInstanceOf[VFS2FileTable] || otherTable.isInstanceOf[VFS2FileTable]) {
      streamFiles.moveFiles(activeTable, otherTable)
    } else {
      fileUtils.moveFiles(activeTable, otherTable)
    }
  }

  override def deleteFiles(activeTable: FileTable, otherTable: FileTable): Unit = activeTable match {
    case source: LocalFileTable => fileUtils.deleteFiles(source, otherTable)
    case source: VFS2FileTable => streamFiles.deleteFiles(source, otherTable)
  }

}

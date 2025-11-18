package com.anjunar.jcommander.manager

import com.anjunar.jcommander.components.AbstractFileTableComponent
import com.anjunar.jcommander.files.LinuxFileUtils

import java.awt.image.BufferedImage

class LinuxFileManager extends FileManager {

  val fileUtils = new LinuxFileUtils()

  override def fileContext(activeTable: AbstractFileTableComponent): Unit = ???

  override def getFileIcon(file: String, large: Boolean): BufferedImage = fileUtils.getFileIcon(file, large)

  override def executeFile(file: String, workingDir: String, args: Seq[String]): Unit = ???

  override def console(activeTable: AbstractFileTableComponent): Unit = ???

  override def executeFile(activeTable: AbstractFileTableComponent): Unit = ???

  override def mkDir(activeTable: AbstractFileTableComponent): Unit = ???

  override def renameFile(activeTable: AbstractFileTableComponent): Unit = ???

  override def copyFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit = ???

  override def moveFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit = ???

  override def deleteFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit = ???
}

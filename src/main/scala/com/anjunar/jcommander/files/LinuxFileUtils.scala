package com.anjunar.jcommander.files

import com.anjunar.jcommander.LinuxNativeCopy
import com.anjunar.jcommander.components.AbstractFileTableComponent

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File}
import javax.imageio.ImageIO

class LinuxFileUtils extends AbstractFileUtils {

  override def fileContext(files: Seq[String]): Unit = ???

  override def getFileIcon(file: String, large: Boolean): BufferedImage = {
    val bytes = LinuxNativeCopy.getFileIcon(file, large)
    ImageIO.read(new ByteArrayInputStream(bytes))
  }

  override def console(workingDir: String): Unit = ???

  override def executeFile(file: String): Unit = ???

  override def copyFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit = ???

  override def moveFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit = ???

  override def deleteFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit = ???
}

package com.anjunar.jcommander.files

import com.anjunar.jcommander.FileTable
import scalafx.beans.property.{BooleanProperty, ObjectProperty}

import java.awt.image.BufferedImage
import java.io.File

trait FileUtils {
  
  def getFileIcon(file : File, large : Boolean) : BufferedImage
  
  def executeFile(file : File) : Unit

  def mkDir(activeTable: FileTable): Unit

  def renameFile(activeTable: FileTable): Unit

  def copyFiles(activeTable: FileTable, otherTable: FileTable): Unit

  def moveFiles(activeTable: FileTable, otherTable: FileTable): Unit

  def deleteFiles(activeTable: FileTable, otherTable: FileTable): Unit
}

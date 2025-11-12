package com.anjunar.jcommander.files

import com.anjunar.jcommander.FileTable
import scalafx.beans.property.{BooleanProperty, ObjectProperty}

import java.awt.image.BufferedImage
import java.io.File

trait FileUtils {
  
  def getFileIcon(file : File, large : Boolean) : BufferedImage
  
  def executeFile(file : File) : Unit

  def mkDir(activeTable: ObjectProperty[FileTable], darkMode: BooleanProperty): Unit

  def renameFile(activeTable: ObjectProperty[FileTable], darkMode: BooleanProperty): Unit

  def copyFiles(activeTable: ObjectProperty[FileTable],
                otherTable: ObjectProperty[FileTable],
                darkMode: BooleanProperty): Unit

  def moveFiles(activeTable: ObjectProperty[FileTable],
                otherTable: ObjectProperty[FileTable],
                darkMode: BooleanProperty): Unit

  def deleteFiles(activeTable: ObjectProperty[FileTable],
                  otherTable: ObjectProperty[FileTable],
                  darkMode: BooleanProperty): Unit
}

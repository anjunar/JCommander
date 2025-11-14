package com.anjunar.jcommander.files

import com.anjunar.jcommander.components.FileTableComponent
import scalafx.beans.property.{BooleanProperty, ObjectProperty}

import java.awt.image.BufferedImage
import java.io.File

trait FileUtils {
  
  def getFileIcon(file : File, large : Boolean) : BufferedImage

  def executeFile(file : File, workingDir : File, args : Seq[String]) : Unit
  
  def console(workingDir : File) : Unit
  
  def executeFile(file : File) : Unit

  def mkDir(activeTable: FileTableComponent): Unit

  def renameFile(activeTable: FileTableComponent): Unit

  def copyFiles(activeTable: FileTableComponent, otherTable: FileTableComponent): Unit

  def moveFiles(activeTable: FileTableComponent, otherTable: FileTableComponent): Unit

  def deleteFiles(activeTable: FileTableComponent, otherTable: FileTableComponent): Unit
}

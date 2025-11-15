package com.anjunar.jcommander.files

import com.anjunar.jcommander.components.AbstractFileTableComponent
import scalafx.beans.property.{BooleanProperty, ObjectProperty}

import java.awt.image.BufferedImage
import java.io.File

trait FileUtils {
  
  def fileContext(files: Seq[String]) : Unit
  
  def getFileIcon(file : String, large : Boolean) : BufferedImage

  def executeFile(file : String, workingDir : String, args : Seq[String]) : Unit
  
  def console(workingDir : String) : Unit
  
  def executeFile(file : String) : Unit

  def mkDir(activeTable: AbstractFileTableComponent): Unit

  def renameFile(activeTable: AbstractFileTableComponent): Unit

  def copyFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit

  def moveFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit

  def deleteFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit
}

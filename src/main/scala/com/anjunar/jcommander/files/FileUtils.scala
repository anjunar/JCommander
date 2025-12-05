package com.anjunar.jcommander.files

import com.anjunar.jcommander.dsl.FileTable
import javafx.scene.input.MouseEvent

import java.awt.image.BufferedImage
import java.io.File

trait FileUtils {
  
  def fileContext(files: Seq[String], event: MouseEvent): Unit
  
  def getFileIcon(file : String, large : Boolean) : BufferedImage

  def executeFile(file : String, workingDir : String, args : Seq[String]) : Unit
  
  def console(workingDir : String) : Unit
  
  def executeFile(file : String) : Unit

  def mkDir(activeTable: FileTable): Unit

  def renameFile(activeTable: FileTable): Unit

  def copyFiles(activeTable: FileTable, otherTable: FileTable): Unit

  def moveFiles(activeTable: FileTable, otherTable: FileTable): Unit

  def deleteFiles(activeTable: FileTable, otherTable: FileTable): Unit
}

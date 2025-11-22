package com.anjunar.jcommander.manager

import com.anjunar.jcommander.dsl.FileTable
import javafx.scene.input.MouseEvent

import java.awt.image.BufferedImage

trait FileManager {

  def fileContext(activeTable: FileTable, event: MouseEvent): Unit

  def getFileIcon(file : String, large : Boolean): BufferedImage

  def executeFile(file: String, workingDir : String, args: Seq[String]): Unit

  def console(activeTable: FileTable): Unit

  def executeFile(activeTable: FileTable): Unit

  def mkDir(activeTable: FileTable): Unit

  def renameFile(activeTable: FileTable): Unit

  def copyFiles(activeTable: FileTable, otherTable: FileTable): Unit

  def moveFiles(activeTable: FileTable, otherTable: FileTable) : Unit

  def deleteFiles(activeTable: FileTable, otherTable: FileTable): Unit
}

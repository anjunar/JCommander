package com.anjunar.jcommander.manager

import com.anjunar.jcommander.components.AbstractFileTableComponent

import java.awt.image.BufferedImage

trait FileManager {

  def fileContext(activeTable: AbstractFileTableComponent): Unit

  def getFileIcon(file : String, large : Boolean): BufferedImage

  def executeFile(file: String, workingDir : String, args: Seq[String]): Unit

  def console(activeTable: AbstractFileTableComponent): Unit

  def executeFile(activeTable: AbstractFileTableComponent): Unit

  def mkDir(activeTable: AbstractFileTableComponent): Unit

  def renameFile(activeTable: AbstractFileTableComponent): Unit

  def copyFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit

  def moveFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent) : Unit

  def deleteFiles(activeTable: AbstractFileTableComponent, otherTable: AbstractFileTableComponent): Unit
}

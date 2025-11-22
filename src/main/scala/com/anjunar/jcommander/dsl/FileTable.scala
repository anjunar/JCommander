package com.anjunar.jcommander.dsl

import com.anjunar.jcommander.files.FileItem
import javafx.scene.control.TableView
import org.apache.commons.vfs2.{FileObject, FileSystemManager}

import scala.collection.mutable

trait FileTable {

  val node : TableView[FileItem]

  val manager : FileSystemManager
  
  def directory : String

  def resolveDirectory: FileObject

  def loadDirectory(value: String) : Unit

  def lastSelections : mutable.Map[String, String]

}

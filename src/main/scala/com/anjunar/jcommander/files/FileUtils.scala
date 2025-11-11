package com.anjunar.jcommander.files

import com.anjunar.jcommander.FileTable
import scalafx.beans.property.{BooleanProperty, ObjectProperty}

trait FileUtils {

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

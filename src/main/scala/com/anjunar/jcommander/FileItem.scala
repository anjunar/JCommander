package com.anjunar.jcommander

import java.io.File
import java.nio.file.Files

case class FileItem(name: String, ext: String, size: String, date: String, file: File) {
  
  def isReadable : Boolean = Files.isReadable(file.toPath)
  
  def isWriteable : Boolean = Files.isWritable(file.toPath)
  
  def isExecutable : Boolean = Files.isExecutable(file.toPath)

  def isHidden : Boolean = Files.isHidden(file.toPath)
  
}



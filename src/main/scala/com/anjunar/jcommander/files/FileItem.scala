package com.anjunar.jcommander.files

import javafx.beans.property.SimpleObjectProperty
import scalafx.beans.property.ObjectProperty

import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files

case class FileItem(name: String,
                    ext: String,
                    size: String,
                    date: String,
                    file: File,
                    icon: SimpleObjectProperty[BufferedImage] = new SimpleObjectProperty[BufferedImage](null)) {

  def isReadable : Boolean = Files.isReadable(file.toPath)

  def isWriteable : Boolean = Files.isWritable(file.toPath)

  def isExecutable : Boolean = Files.isExecutable(file.toPath)

  def isHidden : Boolean = Files.isHidden(file.toPath)

}



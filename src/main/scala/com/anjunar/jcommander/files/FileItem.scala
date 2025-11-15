package com.anjunar.jcommander.files

import javafx.beans.property.SimpleObjectProperty
import scalafx.beans.property.ObjectProperty

import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.{Files, Path}

case class FileItem(name: String,
                    ext: String,
                    size: String,
                    date: String,
                    file: String,
                    isDir : Boolean,
                    isUpDir : Boolean = false,
                    icon: SimpleObjectProperty[BufferedImage] = new SimpleObjectProperty[BufferedImage](null)) {

  lazy val asJavaFile = new File(file)

}




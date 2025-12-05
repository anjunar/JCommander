package com.anjunar.jcommander.files

import javafx.beans.property.SimpleObjectProperty

import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.{Files, Path}

case class FileItem(name: String,
                    ext: String,
                    size: String,
                    sizeLong: Long,
                    date: String,
                    dateLong: Long,
                    file: String,
                    isDir : Boolean,
                    parent : String,
                    isUpDir : Boolean = false,
                    icon: SimpleObjectProperty[BufferedImage] = new SimpleObjectProperty[BufferedImage](null))




package com.anjunar.jcommander

import java.awt.image.BufferedImage
import java.io.File
import scala.collection.concurrent.TrieMap

object IconCache {
  private val cache = TrieMap[String, BufferedImage]()

  def getOrLoad(file: File, large: Boolean, loader: File => BufferedImage): BufferedImage = {
    val key = if (file.isDirectory) "<DIR>" else file.getName.split('.').lastOption.getOrElse("").toLowerCase
    cache.getOrElseUpdate(key, loader(file))
  }
}
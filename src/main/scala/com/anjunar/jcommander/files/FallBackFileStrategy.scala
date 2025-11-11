package com.anjunar.jcommander.files

import java.nio.file.Path

trait FallBackFileStrategy {
  
  def process(path : Path, target : Path, replaceExisting : Boolean, copyAttributes : Boolean, progressCallback: Double => Unit) : Unit
  
}

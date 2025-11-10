package com.anjunar.jcommander

import java.nio.file.Path

trait FileStrategy {
  
  def process(path : Path, target : Path, replaceExisting : Boolean, copyAttributes : Boolean) : Unit
  
}

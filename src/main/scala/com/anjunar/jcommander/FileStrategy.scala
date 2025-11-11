package com.anjunar.jcommander

import java.nio.file.Path

trait FileStrategy {
  
  def process(path : Path, target : Path, replaceExisting : Boolean, copyAttributes : Boolean, progressCallback: Double => Unit) : Unit
  
  def winProcess(path : Seq[Path], target : Path, progressCallback: WinNativeCopy.ProgressCallback) : Unit
  
}

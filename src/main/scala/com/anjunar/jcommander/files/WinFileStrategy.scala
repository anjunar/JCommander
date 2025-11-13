package com.anjunar.jcommander.files

import com.anjunar.jcommander.WinNativeCopy

import java.nio.file.Path

trait WinFileStrategy {

  def winProcess(path : Seq[Path], target : Path, overwrite : Boolean, recycle : Boolean, progressCallback: WinNativeCopy.ProgressCallback) : Unit

}

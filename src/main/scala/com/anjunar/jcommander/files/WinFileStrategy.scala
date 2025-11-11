package com.anjunar.jcommander.files

import com.anjunar.jcommander.WinNativeCopy

import java.nio.file.Path

trait WinFileStrategy {

  def winProcess(path : Seq[Path], target : Path, progressCallback: WinNativeCopy.ProgressCallback) : Unit

}

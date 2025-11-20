package com.anjunar.jcommander.files

import com.anjunar.jcommander.{LinuxNativeCopy, OSXNativeCopy, WinNativeCopy}

import java.nio.file.Path

trait OSXFileStrategy {

  def winProcess(path : Seq[Path], target : Path, overwrite : Boolean, recycle : Boolean, progressCallback: OSXNativeCopy.ProgressListener) : Unit

}

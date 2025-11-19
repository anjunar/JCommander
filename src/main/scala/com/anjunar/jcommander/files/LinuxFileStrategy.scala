package com.anjunar.jcommander.files

import com.anjunar.jcommander.{LinuxNativeCopy, WinNativeCopy}

import java.nio.file.Path

trait LinuxFileStrategy {

  def winProcess(path : Seq[Path], target : Path, overwrite : Boolean, recycle : Boolean, progressCallback: LinuxNativeCopy.ProgressListener) : Unit

}

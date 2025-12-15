package com.anjunar.jcommander.manager

import com.anjunar.jcommander.utils.OSType

object FileManagerProducer {
  
  def produces() : FileManager = {
    OSType.osName match {
      case "linux" => new LinuxFileManager()
      case "mac" => new OSXFileManager()
      case "win" => new WinFileManager()
    }
  }

}

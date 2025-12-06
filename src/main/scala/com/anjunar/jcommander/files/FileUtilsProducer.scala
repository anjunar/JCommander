package com.anjunar.jcommander.files

import com.anjunar.jcommander.utils.OSType

object FileUtilsProducer {
  
  def produce(): FileUtils = {
    OSType.osName match {
      case "linux" => new LinuxFileUtils()
      case "mac" => new OSXFileUtils()
      case "win" => new WinFileUtils()
    }
  }

}

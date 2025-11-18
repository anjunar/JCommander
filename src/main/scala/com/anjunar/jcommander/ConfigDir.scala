package com.anjunar.jcommander

import com.anjunar.jcommander.utils.OSType

import java.io.File

object ConfigDir {

  def path() : File = {
    OSType.osName match {
      case "linux" => new File(System.getProperty("user.home") + "/.config/jcommander")
      case "mac" => new File(System.getProperty("user.home") + "/Library/Application Support/jcommander")
      case "win" => new File(sys.env("LOCALAPPDATA") + "/jcommander")
    }
  }

}

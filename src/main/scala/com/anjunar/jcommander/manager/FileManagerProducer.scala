package com.anjunar.jcommander.manager

import com.anjunar.jcommander.utils.OSType
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class FileManagerProducer {
  
  @Produces
  def produces() : FileManager = {
    OSType.osName match {
      case "linux" => new LinuxFileManager()
      case "mac" => new OSXFileManager()
      case "win" => new WinFileManager()
    }
  }

}

package com.anjunar.jcommander.files

import com.anjunar.jcommander.OSType
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class FileUtilsProducer {

  @Produces
  def produce(): FileUtils = {
    OSType.osName match {
      case "linux" => new FallBackFileUtils()
      case "mac" => new FallBackFileUtils()
      case "win" => new WinFileUtils()
    }
  }

}

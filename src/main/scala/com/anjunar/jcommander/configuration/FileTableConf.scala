package com.anjunar.jcommander.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.enterprise.context.ApplicationScoped

import java.io.File
import scala.beans.BeanProperty
import scala.compiletime.uninitialized

class FileTableConf {
  
  @JsonProperty("file") 
  @BeanProperty  
  var file : File = new File(System.getProperty("user.home"))
  
}

object FileTableConf {
  
  @ApplicationScoped
  class Left extends FileTableConf

  @ApplicationScoped
  class Right extends FileTableConf
  
}

package com.anjunar.jcommander.configuration

import com.fasterxml.jackson.annotation.JsonProperty

import java.io.File
import scala.beans.BeanProperty
import scala.compiletime.uninitialized

class FileTableConf {
  
  @JsonProperty("file") 
  @BeanProperty  
  var file : File = new File(System.getProperty("user.home"))
  
}

object FileTableConf {
  
  class Left extends FileTableConf
  
  object Left {
    val instance = new Left
    def apply() : Left = instance
  }

  class Right extends FileTableConf
  
  object Right {
    val instance = new Right
    def apply() : Right = instance
  }
  
}

package com.anjunar.jcommander.application

import com.anjunar.jcommander.configuration.Configuration
import com.anjunar.jcommander.objectmapper.ObjectMapperBuilder
import com.fasterxml.jackson.annotation.JsonProperty

import java.io.File

object ConfigurationLoader {
  
  def load() : Configuration = {
    
    val configuration = Configuration()

    val objectMapper = ObjectMapperBuilder.build()

    val configDir = ConfigDir.path()
    val configFile = new File(configDir, "configuration.json")

    objectMapper.readerForUpdating(configuration).readValue(configFile)

    configuration
  }

}

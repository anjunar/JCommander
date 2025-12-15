package com.anjunar.jcommander.commands

import com.anjunar.jcommander.application.ConfigDir
import com.anjunar.jcommander.configuration.Configuration
import com.anjunar.jcommander.objectmapper.ObjectMapperBuilder

import java.io.{File, PrintWriter}
import java.nio.charset.StandardCharsets
import scala.util.Using

class QuitCommand extends Command {

  val configuration: Configuration = Configuration()

  override def canExecute: Boolean = true

  override def execute(): Unit = {
    if (canExecute) {
      val objectMapper = ObjectMapperBuilder.build()

      val configurationString = objectMapper
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(configuration)

      val configDir = ConfigDir.path()
      val configFile = new File(configDir, "configuration.json")

      if (!configDir.exists()) {
        configDir.mkdirs()
      }

      Using.resource(new PrintWriter(configFile, StandardCharsets.UTF_8)) { writer =>
        writer.print(configurationString)
      }

      System.exit(0)
    }
  }
}

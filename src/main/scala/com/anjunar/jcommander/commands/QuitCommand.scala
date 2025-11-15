package com.anjunar.jcommander.commands

import com.anjunar.jcommander.CdiUtils.*
import com.anjunar.jcommander.configuration.Configuration
import com.anjunar.jcommander.objectmapper.ObjectMapperBuilder
import jakarta.enterprise.context.Dependent

import java.io.{File, PrintWriter}
import java.nio.charset.StandardCharsets
import scala.util.Using

@Dependent
class QuitCommand extends Command {

  val configuration: Configuration = inject(classOf[Configuration])

  override def canExecute: Boolean = true

  override def execute(): Unit = {
    if (canExecute) {
      val objectMapper = ObjectMapperBuilder.build()

      val configurationString = objectMapper
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(configuration)

      val homeDir = System.getProperty("user.home")
      val configDir = new File(homeDir, ".jcommander")
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

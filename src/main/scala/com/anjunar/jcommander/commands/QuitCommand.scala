package com.anjunar.jcommander.commands

import com.anjunar.jcommander.components.PrimaryStage
import com.anjunar.jcommander.configuration.Configuration
import com.anjunar.jcommander.objectmapper.{CdiModule, ObjectMapperBuilder}
import com.anjunar.jcommander.inject
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.Dependent

import java.io.{File, PrintWriter}
import java.nio.charset.StandardCharsets
import scala.util.Using

@Dependent
class QuitCommand extends Command {

  val configuration: Configuration = inject(classOf[Configuration])
  val primaryStage: PrimaryStage = inject(classOf[PrimaryStage])

  override def canExecute: Boolean = true

  override def execute(): Unit = {
    if (canExecute) {
      configuration.primaryStage.width = primaryStage.node.width.value
      configuration.primaryStage.height = primaryStage.node.height.value
      
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

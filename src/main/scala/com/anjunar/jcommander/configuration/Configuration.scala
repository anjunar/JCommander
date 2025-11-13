package com.anjunar.jcommander.configuration

import com.anjunar.jcommander.configuration
import com.anjunar.jcommander.objectmapper.ObjectMapperBuilder
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer, JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.spi.CDI
import jakarta.inject.Inject
import org.jboss.weld.proxy.WeldClientProxy

import scala.compiletime.uninitialized

@ApplicationScoped
class Configuration {

  @JsonProperty("darkMode")
  var darkMode: Boolean = true

  @Inject
  @JsonProperty("primaryStage")
  var primaryStage: PrimaryStageConf = uninitialized

  @Inject
  @JsonProperty("sublime")
  var sublimeConf: SublimeConf = uninitialized

  def load(value: Configuration): Unit = {
    darkMode = value.darkMode
    primaryStage.load(value.primaryStage)
    sublimeConf.load(value.sublimeConf)
  }

}

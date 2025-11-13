package com.anjunar.jcommander.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

import scala.compiletime.uninitialized

@ApplicationScoped
class Configuration {

  @Inject
  @JsonProperty("primaryStage")
  var primaryStage: PrimaryStageConf = uninitialized

  @Inject
  @JsonProperty("sublime")
  var sublimeConf: SublimeConf = uninitialized

}

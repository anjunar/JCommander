package com.anjunar.jcommander.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

import scala.beans.BeanProperty
import scala.compiletime.uninitialized

@ApplicationScoped
class Configuration {

  @Inject
  @BeanProperty
  @JsonProperty("primaryStage")
  var primaryStage: PrimaryStageConf = uninitialized

  @Inject
  @BeanProperty
  @JsonProperty("sublime")
  var sublimeConf: SublimeConf = uninitialized

}

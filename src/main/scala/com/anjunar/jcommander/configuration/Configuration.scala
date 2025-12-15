package com.anjunar.jcommander.configuration

import com.fasterxml.jackson.annotation.JsonProperty

import scala.beans.BeanProperty
import scala.compiletime.uninitialized

class Configuration {

  @BeanProperty
  @JsonProperty("primaryStage")
  var primaryStage: PrimaryStageConf = PrimaryStageConf()

  @BeanProperty
  @JsonProperty("textEditor")
  var textEditor: TextEditorConf = TextEditorConf()

}

object Configuration {
  val instance = new Configuration
  def apply() : Configuration = instance
}
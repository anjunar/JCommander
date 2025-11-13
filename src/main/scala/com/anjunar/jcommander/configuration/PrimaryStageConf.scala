package com.anjunar.jcommander.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.enterprise.context.ApplicationScoped

import scala.beans.BeanProperty

@ApplicationScoped
class PrimaryStageConf {

  @JsonProperty("width")
  var width: Double = 1100

  @JsonProperty("height")
  var height: Double = 600
  
  def load(value : PrimaryStageConf): Unit = {
    width = value.width
    height = value.height
  }

}
package com.anjunar.jcommander.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.enterprise.context.ApplicationScoped
import scalafx.beans.property.BooleanProperty

import scala.beans.BeanProperty

@ApplicationScoped
class DarkModeConf {

  @JsonProperty("value")
  @BeanProperty  
  var value : Boolean = true
  
  val valueProperty = new BooleanProperty {}

}

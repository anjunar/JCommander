package com.anjunar.jcommander.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.enterprise.context.ApplicationScoped
import javafx.beans.property.SimpleBooleanProperty

import scala.beans.BeanProperty

@ApplicationScoped
class DarkModeConf {

  @JsonProperty("value")
  @BeanProperty  
  var value : Boolean = false
  
  val valueProperty = new SimpleBooleanProperty(value)

}

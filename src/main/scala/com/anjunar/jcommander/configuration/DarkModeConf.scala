package com.anjunar.jcommander.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.enterprise.context.ApplicationScoped
import javafx.beans.property.SimpleBooleanProperty

import scala.beans.BeanProperty

@ApplicationScoped
class DarkModeConf {

  val valueProperty = new SimpleBooleanProperty(true)

  @JsonProperty("value")
  def getValue() : Boolean = valueProperty.getValue
  def setValue(value : Boolean): Unit = valueProperty.setValue(value)
  
}

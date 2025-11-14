package com.anjunar.jcommander.configuration

import com.anjunar.jcommander.CdiUtils.*
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

import scala.beans.BeanProperty
import scala.compiletime.uninitialized

@ApplicationScoped
class PrimaryStageConf {

  @BeanProperty
  @JsonProperty("width")
  var width: Double = 1100

  @BeanProperty
  @JsonProperty("height")
  var height: Double = 600
  
  @Inject
  @BeanProperty
  @JsonProperty("darkMode")
  var darkMode: DarkModeConf = uninitialized

  @Inject
  @BeanProperty
  @JsonProperty("leftFileTable")
  var leftTable : FileTableConf.Left = uninitialized

  @Inject
  @BeanProperty
  @JsonProperty("rightFileTable")
  var rightTable: FileTableConf.Right = uninitialized


}
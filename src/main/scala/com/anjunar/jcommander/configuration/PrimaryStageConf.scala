package com.anjunar.jcommander.configuration

import com.fasterxml.jackson.annotation.JsonProperty

import scala.beans.BeanProperty
import scala.compiletime.uninitialized

class PrimaryStageConf {

  @BeanProperty
  @JsonProperty("width")
  var width: Double = 1100

  @BeanProperty
  @JsonProperty("height")
  var height: Double = 600

  @BeanProperty
  @JsonProperty("x")
  var x: Double = 0

  @BeanProperty
  @JsonProperty("y")
  var y: Double = 0
  
  @BeanProperty
  @JsonProperty("darkMode")
  var darkMode: DarkModeConf = DarkModeConf()

  @BeanProperty
  @JsonProperty("leftFileTable")
  var leftTable : FileTableConf.Left = FileTableConf.Left()

  @BeanProperty
  @JsonProperty("rightFileTable")
  var rightTable: FileTableConf.Right = FileTableConf.Right()


}

object PrimaryStageConf {
  val instance = new PrimaryStageConf
  def apply() : PrimaryStageConf = instance
}
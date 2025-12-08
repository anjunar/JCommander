package com.anjunar.jcommander.configuration

import com.fasterxml.jackson.annotation.JsonProperty

import scala.beans.BeanProperty

class TextEditorConf {

  @BeanProperty
  @JsonProperty("executable")
  var executable = "C:\\Program Files\\Sublime Text\\sublime_text.exe"

}

object TextEditorConf {
  val instance = new TextEditorConf
  def apply() : TextEditorConf = instance
}
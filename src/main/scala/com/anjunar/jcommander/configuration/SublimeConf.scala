package com.anjunar.jcommander.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.enterprise.context.ApplicationScoped

import scala.beans.BeanProperty

@ApplicationScoped
class SublimeConf {

  @JsonProperty("executable")
  var executable = "C:\\Program Files\\Sublime Text\\sublime_text.exe"

  def load(value : SublimeConf): Unit = {
    executable = value.executable
  }

}
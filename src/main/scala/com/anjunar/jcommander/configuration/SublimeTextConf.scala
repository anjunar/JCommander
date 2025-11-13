package com.anjunar.jcommander.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.enterprise.context.ApplicationScoped

import scala.beans.BeanProperty

@ApplicationScoped
class SublimeTextConf {

  @BeanProperty
  @JsonProperty("executable")
  var executable = "C:\\Program Files\\Sublime Text\\sublime_text.exe"

}
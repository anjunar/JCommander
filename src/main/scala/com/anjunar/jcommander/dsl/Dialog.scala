package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.ElementBuilder
import scalafx.stage.Stage

class Dialog extends ElementBuilder[Stage]{
  
  lazy val node : Stage = new Stage()
  
  override def build(): Stage = node
}

package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.*
import com.anjunar.javafx.dsl.{ChildNodeBuilder, ElementBuilder, NodeBuilder, Producer}
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.scene.{Node, control, layout}
import javafx.scene.control.Button

class button extends NodeBuilder[control.Button], HasLabeled, HasText, HasOnAction, HasWidth, HasHeight {
  
  lazy val node: control.Button = new Button()

  override def build(): Button = node
  
}

object button extends Producer[button, control.Button]{
  override def createBuilder: button = new button()
}

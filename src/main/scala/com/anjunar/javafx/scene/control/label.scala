package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.{HasLabeled, HasText}
import com.anjunar.javafx.dsl.{BuildContext, ChildBuilder, DSL, ElementBuilder, Producer, Ref}
import javafx.scene.Node
import javafx.scene.control

class label extends ElementBuilder[control.Label], HasLabeled, HasText {
  lazy val node: control.Label = new control.Label()
  override def build(): control.Label = node
}

object label extends Producer[label, control.Label]{

  override def createBuilder: label = new label()
}

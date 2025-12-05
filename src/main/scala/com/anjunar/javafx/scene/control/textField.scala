package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.{HasText, HasWidth, IstTextInput}
import com.anjunar.javafx.dsl.{BuildContext, ElementBuilder, NodeBuilder, Producer}
import javafx.scene.Node
import javafx.scene.control.TextField

class textField extends NodeBuilder[TextField], HasText, IstTextInput, HasWidth {
  override lazy val node: TextField = new TextField()

  override def build(): TextField = node
}

object textField extends Producer[textField, TextField] {

  override def createBuilder: textField = new textField()

}

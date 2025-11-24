package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.HasText
import com.anjunar.javafx.dsl.{NodeBuilder, Producer}
import javafx.scene.Node
import javafx.scene.control.TextField

class textField extends NodeBuilder[TextField], HasText {
  override lazy val node: TextField = new TextField()

  override def build(): TextField = node
}

object textField extends Producer[textField, TextField] {

  override def createBuilder: textField = new textField()
  
  def promptText(using tf: textField): String = tf.node.getPromptText
  def promptText_=(v: String)(using tf: textField): Unit = tf.node.setPromptText(v)
}

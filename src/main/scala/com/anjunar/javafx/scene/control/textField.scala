package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.HasText
import com.anjunar.javafx.dsl.{BuildContext, ElementBuilder, NodeBuilder, Producer}
import javafx.scene.Node
import javafx.scene.control.TextField

class textField extends NodeBuilder[TextField], HasText {
  override lazy val node: TextField = new TextField()

  override def build(): TextField = node
}

object textField extends Producer[textField, TextField] {

  override def createBuilder: textField = new textField()

  def promptText(using tf: textField & ElementBuilder[?], ctx : BuildContext): String =
    tf.read(tf.node.getPromptText)
  def promptText_=(v: String)(using tf: textField & ElementBuilder[?], ctx : BuildContext): Unit =
    tf.write(() => tf.node.setPromptText(v))
}

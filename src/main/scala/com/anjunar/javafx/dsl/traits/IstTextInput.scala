package com.anjunar.javafx.dsl.traits

import com.anjunar.javafx.dsl.{BuildContext, ElementBuilder}
import javafx.scene.control.TextInputControl

trait IstTextInput {
  
  lazy val node : TextInputControl
  
}

object IstTextInput {
  def promptText(using tf: IstTextInput & ElementBuilder[?], ctx: BuildContext): String =
    tf.read(tf.node.getPromptText)

  def promptText_=(v: String)(using tf: IstTextInput & ElementBuilder[?], ctx: BuildContext): Unit =
    tf.write(() => tf.node.setPromptText(v))
}

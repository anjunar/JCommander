package com.anjunar.javafx.dsl.traits

trait HasText {
  def getText(): String

  def setText(v: String): Unit
}
object HasText {
  def text()(using h: HasText): String = h.getText()

  def text_=(v: String)(using h: HasText): Unit = h.setText(v)
}
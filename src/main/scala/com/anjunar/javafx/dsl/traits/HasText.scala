package com.anjunar.javafx.dsl.traits

import javafx.beans.property.StringProperty

import scala.language.reflectiveCalls

trait HasText {
  lazy val node : AnyRef {
    def textProperty(): StringProperty

    def getText(): String
    def setText(v: String): Unit
  }
}

object HasText {
  

  def textProperty(using h: HasText): StringProperty = h.node.textProperty()
  
  def text()(using h: HasText): String = h.node.getText()

  def text_=(v: String)(using h: HasText): Unit = h.node.setText(v)
}
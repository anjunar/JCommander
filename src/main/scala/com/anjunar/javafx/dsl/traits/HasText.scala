package com.anjunar.javafx.dsl.traits

import javafx.beans.property.StringProperty
import javafx.scene.Node

import scala.language.reflectiveCalls

trait HasText {
  val node : AnyRef
}

object HasText {
  
  private type T = {
  
    def textProperty(): StringProperty
  
    def getText(): String

    def setText(v: String): Unit
  }

  private inline def t(using h: HasText): T =
    h.node.asInstanceOf[T]

  def textProperty(using h: HasText): StringProperty = t.textProperty()
  
  def text()(using h: HasText): String = t.getText()

  def text_=(v: String)(using h: HasText): Unit = t.setText(v)
}
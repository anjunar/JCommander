package com.anjunar.javafx.dsl.traits

import com.anjunar.javafx.dsl.{BuildContext, ElementBuilder}
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
  
  def textProperty(using h: HasText & ElementBuilder[?], ctx : BuildContext): (StringProperty => Unit) => Unit = {
    (f: StringProperty => Unit) => h.write( () => f(h.node.textProperty() ))
  }

  def text(using h: HasText & ElementBuilder[?], ctx : BuildContext): String = 
    h.read(h.node.getText())

  def text_=(v: String)(using h: HasText & ElementBuilder[?], ctx : BuildContext): Unit =
    h.write(() => h.node.setText(v))
}
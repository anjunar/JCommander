package com.anjunar.javafx.dsl.traits

import com.anjunar.javafx.dsl.{BuildContext, ElementBuilder}
import javafx.collections.ObservableList

import scala.collection.mutable
import scala.language.reflectiveCalls
import scala.jdk.CollectionConverters.*

trait HasStyle {
  
  lazy val node: AnyRef {
    def getStyleClass() : ObservableList[String]
  
    def getStyle() : String
    def setStyle(v : String) : Unit
  }
  
}

object HasStyle {

  def css()(using h: HasStyle & ElementBuilder[?], ctx : BuildContext): mutable.Buffer[String] = 
    h.read(h.node.getStyleClass().asScala)
  def css_=(values: mutable.Buffer[String])(using h: HasStyle & ElementBuilder[?], ctx : BuildContext): Unit = {
    h.write(() => {
      h.node.getStyleClass().clear()
      h.node.getStyleClass().asScala.addAll(values)
    })
  }

  def style()(using h: HasStyle & ElementBuilder[?], ctx : BuildContext): String = 
    h.read(h.node.getStyle())
  def style_=(v: String)(using h: HasStyle & ElementBuilder[?], ctx : BuildContext): Unit =
    h.write(() => h.node.setStyle(v))

}

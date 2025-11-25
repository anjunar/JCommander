package com.anjunar.javafx.dsl.traits

import com.anjunar.javafx.dsl.{BuildContext, ElementBuilder}
import javafx.geometry.Insets

import scala.language.reflectiveCalls
import scala.reflect.Selectable.reflectiveSelectable

trait HasPadding {

  lazy val node: AnyRef {
    def getPadding(): Insets
    def setPadding(value: Insets): Unit
  }


}

object HasPadding {

  def padding()(using h: HasPadding & ElementBuilder[?], ctx : BuildContext): Insets = 
    h.read(h.node.getPadding())

  def padding_=(v: Insets)(using h: HasPadding & ElementBuilder[?], ctx : BuildContext): Unit = 
    h.write(() => h.node.setPadding(v))


}

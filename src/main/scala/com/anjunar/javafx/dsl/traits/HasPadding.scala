package com.anjunar.javafx.dsl.traits

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

  def padding()(using h: HasPadding): Insets = h.node.getPadding()

  def padding_=(v: Insets)(using h: HasPadding): Unit = h.node.setPadding(v)


}

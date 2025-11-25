package com.anjunar.javafx.dsl.traits

import com.anjunar.javafx.dsl.BuildContext

import scala.language.reflectiveCalls

trait HasHeight {

  lazy val node: AnyRef {
    def getPrefHeight(): Double
    def setPrefHeight(v: Double): Unit

    def getMinHeight(): Double
    def setMinHeight(v: Double): Unit

    def getMaxHeight(): Double
    def setMaxHeight(v: Double): Unit
  }

}

object HasHeight {

  def prefHeight()(using h: HasHeight, b : BuildContext): Double = h.node.getPrefHeight()
  def prefHeight_=(v: Double)(using h: HasHeight, b : BuildContext): Unit = h.node.setPrefHeight(v)

  def maxHeight()(using h: HasHeight, b : BuildContext): Double = h.node.getMaxHeight()
  def maxHeight_=(v: Double)(using h: HasHeight, b : BuildContext): Unit = h.node.setMaxHeight(v)

  def minHeight()(using h: HasHeight, b : BuildContext): Double = h.node.getMinHeight()
  def minHeight_=(v: Double)(using h: HasHeight, b : BuildContext): Unit = h.node.setMinHeight(v)
}

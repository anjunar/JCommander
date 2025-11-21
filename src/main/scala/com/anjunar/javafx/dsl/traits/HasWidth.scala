package com.anjunar.javafx.dsl.traits

import javafx.scene.layout.Region

trait HasWidth {
  
  def getPrefWidth() : Double
  def setPrefWidth(v : Double) : Unit

  def getMinWidth(): Double
  def setMinWidth(v: Double): Unit

  def getMaxWidth(): Double
  def setMaxWidth(v: Double): Unit


}

object HasWidth {
  def prefWidth()(using h: HasWidth): Double = h.getPrefWidth()
  def prefWidth_=(v: Double)(using h: HasWidth): Unit = h.setPrefWidth(v)

  def maxWidth()(using h: HasWidth): Double = h.getMaxWidth()
  def maxWidth_=(v: Double)(using h: HasWidth): Unit = h.setMaxWidth(v)

  def minWidth()(using h: HasWidth): Double = h.getMinWidth()
  def minWidth_=(v: Double)(using h: HasWidth): Unit = h.setMinWidth(v)


}

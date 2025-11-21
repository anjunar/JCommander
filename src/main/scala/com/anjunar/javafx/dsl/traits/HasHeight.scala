package com.anjunar.javafx.dsl.traits

trait HasHeight {

  def getPrefHeight(): Double
  def setPrefHeight(v: Double): Unit

  def getMinHeight(): Double
  def setMinHeight(v: Double): Unit

  def getMaxHeight(): Double
  def setMaxHeight(v: Double): Unit
  
}

object HasHeight {
  def prefHeight()(using h: HasHeight): Double = h.getPrefHeight()
  def prefHeight_=(v: Double)(using h: HasHeight): Unit = h.setPrefHeight(v)

  def maxHeight()(using h: HasHeight): Double = h.getMaxHeight()
  def maxHeight_=(v: Double)(using h: HasHeight): Unit = h.setMaxHeight(v)

  def minHeight()(using h: HasHeight): Double = h.getMinHeight()
  def minHeight_=(v: Double)(using h: HasHeight): Unit = h.setMinHeight(v)
}

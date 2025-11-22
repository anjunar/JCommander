package com.anjunar.javafx.dsl.traits

import javafx.beans.property.SimpleDoubleProperty

import scala.language.reflectiveCalls

trait HasHeight {

  var prefHeightProperty = new SimpleDoubleProperty(-1)
  var maxHeightProperty = new SimpleDoubleProperty(-1)
  var minHeightProperty = new SimpleDoubleProperty(-1)

}

object HasHeight {

  def prefHeight()(using h: HasHeight): Double = h.prefHeightProperty.get()

  def prefHeight_=(v: Double)(using h: HasHeight): Unit = h.prefHeightProperty.set(v)

  def maxHeight()(using h: HasHeight): Double = h.maxHeightProperty.get()

  def maxHeight_=(v: Double)(using h: HasHeight): Unit = h.maxHeightProperty.set(v)

  def minHeight()(using h: HasHeight): Double = h.minHeightProperty.get()

  def minHeight_=(v: Double)(using h: HasHeight): Unit = h.minHeightProperty.set(v)
}

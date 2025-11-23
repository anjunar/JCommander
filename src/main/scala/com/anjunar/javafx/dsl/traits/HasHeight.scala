package com.anjunar.javafx.dsl.traits

import javafx.beans.property.SimpleDoubleProperty

import scala.language.reflectiveCalls

trait HasHeight {

  var prefHeightProperty: Option[SimpleDoubleProperty] = None
  var maxHeightProperty:  Option[SimpleDoubleProperty] = None
  var minHeightProperty:  Option[SimpleDoubleProperty] = None

}

object HasHeight {

  def prefHeight(using h: HasHeight): Double =
    h.prefHeightProperty.map(_.get).getOrElse(-1)

  def prefHeight_=(v: Double)(using h: HasHeight): Unit =
    h.prefHeightProperty match {
      case Some(p) => p.set(v)
      case None    => h.prefHeightProperty = Some(SimpleDoubleProperty(v))
    }

  def maxHeight(using h: HasHeight): Double =
    h.maxHeightProperty.map(_.get).getOrElse(-1)

  def maxHeight_=(v: Double)(using h: HasHeight): Unit =
    h.maxHeightProperty match {
      case Some(p) => p.set(v)
      case None    => h.maxHeightProperty = Some(SimpleDoubleProperty(v))
    }

  def minHeight(using h: HasHeight): Double =
    h.minHeightProperty.map(_.get).getOrElse(-1)

  def minHeight_=(v: Double)(using h: HasHeight): Unit =
    h.minHeightProperty match {
      case Some(p) => p.set(v)
      case None    => h.minHeightProperty = Some(SimpleDoubleProperty(v))
    }
}

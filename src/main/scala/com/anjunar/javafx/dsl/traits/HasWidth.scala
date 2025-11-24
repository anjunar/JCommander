package com.anjunar.javafx.dsl.traits

import javafx.beans.property.{DoubleProperty, ReadOnlyDoubleProperty}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.Node

import scala.language.reflectiveCalls

trait HasWidth {

  lazy val node : AnyRef {
    def widthProperty(): ReadOnlyDoubleProperty

    def getWidth(): Double

    def getPrefWidth() : Double
    def setPrefWidth(v : Double) : Unit

    def getMinWidth(): Double
    def setMinWidth(v: Double): Unit

    def getMaxWidth(): Double
    def setMaxWidth(v: Double): Unit
  }

}

object HasWidth {

  def widthProperty()(using h: HasWidth): ReadOnlyDoubleProperty = h.node.widthProperty()
  
  def width()(using h: HasWidth): Double = h.node.getWidth()

  def prefWidth()(using h: HasWidth): Double = h.node.getPrefWidth()
  def prefWidth_=(v: Double)(using h: HasWidth): Unit = h.node.setPrefWidth(v)

  def maxWidth()(using h: HasWidth): Double = h.node.getMaxWidth()
  def maxWidth_=(v: Double)(using h: HasWidth): Unit = h.node.setMaxWidth(v)

  def minWidth()(using h: HasWidth): Double = h.node.getMinWidth()
  def minWidth_=(v: Double)(using h: HasWidth): Unit = h.node.setMinWidth(v)


}

package com.anjunar.javafx.dsl.traits

import javafx.beans.property.{DoubleProperty, ReadOnlyDoubleProperty}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.Node

import scala.language.reflectiveCalls

trait HasWidth {

  val node : AnyRef

}

object HasWidth {

  private type W = {
    def widthProperty(): ReadOnlyDoubleProperty
      
    def getWidth(): Double

    def getPrefWidth() : Double
    def setPrefWidth(v : Double) : Unit

    def getMinWidth(): Double
    def setMinWidth(v: Double): Unit

    def getMaxWidth(): Double
    def setMaxWidth(v: Double): Unit
  }

  private inline def w(using h: HasWidth): W =
    h.node.asInstanceOf[W]

  def widthProperty()(using h: HasWidth): ReadOnlyDoubleProperty = w.widthProperty()
  
  def width()(using h: HasWidth): Double = w.getWidth()

  def prefWidth()(using h: HasWidth): Double = w.getPrefWidth()
  def prefWidth_=(v: Double)(using h: HasWidth): Unit = w.setPrefWidth(v)

  def maxWidth()(using h: HasWidth): Double = w.getMaxWidth()
  def maxWidth_=(v: Double)(using h: HasWidth): Unit = w.setMaxWidth(v)

  def minWidth()(using h: HasWidth): Double = w.getMinWidth()
  def minWidth_=(v: Double)(using h: HasWidth): Unit = w.setMinWidth(v)


}

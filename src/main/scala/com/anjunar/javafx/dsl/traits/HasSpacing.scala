package com.anjunar.javafx.dsl.traits

import javafx.geometry.{Insets, Pos}

import scala.language.reflectiveCalls

trait HasSpacing {
  lazy val node : AnyRef {
    def getSpacing(): Double
    def setSpacing(v: Double): Unit

    def getPadding() : Insets
    def setPadding(value : Insets) : Unit

    def getAlignment() : Pos
    def setAlignment(value : Pos) : Unit
  }
}
object HasSpacing {
  
  def spacing()(using h: HasSpacing): Double = h.node.getSpacing()
  def spacing_=(v: Double)(using h: HasSpacing): Unit = h.node.setSpacing(v)

  def padding()(using h: HasSpacing): Insets = h.node.getPadding()
  def padding_=(v: Insets)(using h: HasSpacing): Unit = h.node.setPadding(v)

  def alignment()(using h: HasSpacing): Pos = h.node.getAlignment()
  def alignment_=(v: Pos)(using h: HasSpacing): Unit = h.node.setAlignment(v)

}
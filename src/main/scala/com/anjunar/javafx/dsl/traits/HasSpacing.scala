package com.anjunar.javafx.dsl.traits

import javafx.geometry.Pos

trait HasSpacing {
  def getSpacing(): Double
  def setSpacing(v: Double): Unit
  
  def getAlignment() : Pos
  def setAlignment(value : Pos) : Unit
}
object HasSpacing {

  def spacing()(using h: HasSpacing): Double = h.getSpacing()
  def spacing_=(v: Double)(using h: HasSpacing): Unit = h.setSpacing(v)

  def alignment()(using h: HasSpacing): Pos = h.getAlignment()
  def alignment_=(v: Pos)(using h: HasSpacing): Unit = h.setAlignment(v)

}
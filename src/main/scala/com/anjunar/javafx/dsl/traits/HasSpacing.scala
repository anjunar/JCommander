package com.anjunar.javafx.dsl.traits

import javafx.geometry.Pos
import javafx.geometry.Insets

import scala.language.reflectiveCalls

trait HasSpacing {
  lazy val node : AnyRef
}
object HasSpacing {
  
  private type H = {
    def getSpacing(): Double
    def setSpacing(v: Double): Unit
  
    def getPadding() : Insets
    def setPadding(value : Insets) : Unit

    def getAlignment() : Pos
    def setAlignment(value : Pos) : Unit
  }

  private inline def w(using h: HasSpacing): H =
    h.node.asInstanceOf[H]


  def spacing()(using h: HasSpacing): Double = w.getSpacing()
  def spacing_=(v: Double)(using h: HasSpacing): Unit = w.setSpacing(v)
  
  def padding()(using h: HasSpacing): Insets = w.getPadding()
  def padding_=(v: Insets)(using h: HasSpacing): Unit = w.setPadding(v)

  def alignment()(using h: HasSpacing): Pos = w.getAlignment()
  def alignment_=(v: Pos)(using h: HasSpacing): Unit = w.setAlignment(v)

}
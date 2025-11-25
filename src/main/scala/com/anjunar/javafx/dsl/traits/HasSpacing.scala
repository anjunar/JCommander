package com.anjunar.javafx.dsl.traits

import com.anjunar.javafx.dsl.BuildContext
import javafx.geometry.{Insets, Pos}

import scala.language.reflectiveCalls

trait HasSpacing {
  lazy val node : AnyRef {
    def getSpacing(): Double
    def setSpacing(v: Double): Unit

    def getAlignment() : Pos
    def setAlignment(value : Pos) : Unit
  }
}
object HasSpacing {
  
  def spacing()(using h: HasSpacing, b : BuildContext): Double = h.node.getSpacing()
  def spacing_=(v: Double)(using h: HasSpacing, b : BuildContext): Unit = h.node.setSpacing(v)

  def alignment()(using h: HasSpacing, b : BuildContext): Pos = h.node.getAlignment()
  def alignment_=(v: Pos)(using h: HasSpacing, b : BuildContext): Unit = h.node.setAlignment(v)

}
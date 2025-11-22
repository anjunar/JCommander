package com.anjunar.javafx.dsl.traits

import javafx.scene.Node

import scala.language.reflectiveCalls

trait HasHeight {
  
  val node : AnyRef
  
}

object HasHeight {
  
  private type H = {
    def getPrefHeight(): Double
    def setPrefHeight(v: Double): Unit

    def getMinHeight(): Double
    def setMinHeight(v: Double): Unit

    def getMaxHeight(): Double
    def setMaxHeight(v: Double): Unit
  }

  private inline def w(using h: HasHeight): H =
    h.node.asInstanceOf[H]

  def prefHeight()(using h: HasHeight): Double = w.getPrefHeight()
  def prefHeight_=(v: Double)(using h: HasHeight): Unit = w.setPrefHeight(v)

  def maxHeight()(using h: HasHeight): Double = w.getMaxHeight()
  def maxHeight_=(v: Double)(using h: HasHeight): Unit = w.setMaxHeight(v)

  def minHeight()(using h: HasHeight): Double = w.getMinHeight()
  def minHeight_=(v: Double)(using h: HasHeight): Unit = w.setMinHeight(v)
}

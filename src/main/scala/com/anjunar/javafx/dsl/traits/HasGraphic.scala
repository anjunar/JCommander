package com.anjunar.javafx.dsl.traits

import javafx.scene.Node

import scala.language.reflectiveCalls

trait HasGraphic {
  lazy val node: AnyRef {
    def getGraphic(): Node
    def setGraphic(v: Node): Unit
  }
}

object HasGraphic {
  def graphic()(using h: HasGraphic): Node = h.node.getGraphic()

  def graphic_=(v: Node)(using h: HasGraphic): Unit = h.node.setGraphic(v)
}
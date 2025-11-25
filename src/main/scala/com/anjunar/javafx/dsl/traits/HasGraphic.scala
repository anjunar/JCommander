package com.anjunar.javafx.dsl.traits

import com.anjunar.javafx.dsl.BuildContext
import javafx.scene.Node

import scala.language.reflectiveCalls

trait HasGraphic {
  lazy val node: AnyRef {
    def getGraphic(): Node
    def setGraphic(v: Node): Unit
  }
}

object HasGraphic {
  def graphic()(using h: HasGraphic, b : BuildContext): Node = h.node.getGraphic()

  def graphic_=(v: Node)(using h: HasGraphic, b : BuildContext): Unit = h.node.setGraphic(v)
}
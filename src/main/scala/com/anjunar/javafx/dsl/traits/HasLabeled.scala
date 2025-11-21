package com.anjunar.javafx.dsl.traits

import javafx.scene.Node
import javafx.scene.control.Labeled

trait HasLabeled {
  val node : Labeled
}

object HasLabeled {
  def graphic()(using h: HasLabeled): Node = h.node.getGraphic()
  def graphic_=(v: Node)(using h: HasLabeled): Unit = h.node.setGraphic(v)
}
package com.anjunar.javafx.dsl.traits

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.control.Labeled

trait IsLabeled {
  lazy val node : Labeled
}

object IsLabeled {
  def graphic()(using h: IsLabeled): Node = h.node.getGraphic()
  def graphic_=(v: Node)(using h: IsLabeled): Unit = h.node.setGraphic(v)
}
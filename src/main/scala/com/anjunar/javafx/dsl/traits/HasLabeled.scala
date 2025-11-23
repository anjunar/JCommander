package com.anjunar.javafx.dsl.traits

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.control.Labeled

trait HasLabeled {
  
  val graphicProperty: SimpleObjectProperty[Node] = SimpleObjectProperty[Node]()
  
}

object HasLabeled {
  def graphic()(using h: HasLabeled): Node = h.graphicProperty.get()
  def graphic_=(v: Node)(using h: HasLabeled): Unit = h.graphicProperty.set(v)
}
package com.anjunar.javafx.dsl.traits

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.control.Labeled

trait HasLabeled {
  
  var graphicProperty: Option[SimpleObjectProperty[Node]] = None
  
}

object HasLabeled {

  def graphic(using h: HasLabeled): Option[Node] =
    h.graphicProperty.map(_.get)

  def graphic_=(v: Node)(using h: HasLabeled): Unit =
    h.graphicProperty match {
      case Some(p) => p.set(v)
      case None    => h.graphicProperty = Some(SimpleObjectProperty(v))
    }
}

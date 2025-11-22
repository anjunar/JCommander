package com.anjunar.javafx.dsl

import javafx.scene.Node

trait ChildBuilder[C <: Node] extends NodeBuilder[C] {
  def add(child: ElementBuilder[?]): Unit
}

object ChildBuilder {
  
  def addComponent(builder : ElementBuilder[?])(using h: ChildBuilder[?]) : Unit = h.add(builder)
  
}
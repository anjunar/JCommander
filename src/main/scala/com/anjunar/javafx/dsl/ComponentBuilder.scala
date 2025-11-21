package com.anjunar.javafx.dsl

import javafx.scene.Node

import scala.compiletime.uninitialized

class ComponentBuilder[C <: Node](val name: String) extends ElementBuilder[C] {
  var children: List[ElementBuilder[?]] = Nil
  var rootNode: C = uninitialized

  def add(child: ElementBuilder[?]): Unit =
    children = children :+ child
    if rootNode == null then
      rootNode = child.build().asInstanceOf[C]
    {}

  override def build(): C =
    rootNode
}
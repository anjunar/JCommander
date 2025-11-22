package com.anjunar.javafx.dsl

import javafx.scene.Node

import scala.compiletime.uninitialized

class ComponentBuilder[C] extends ElementBuilder[C] {
  var children: List[ElementBuilder[?]] = Nil

  def add(child: ElementBuilder[?]): Unit =
    children = children :+ child

  override def build(): C = {
    children.head.build().asInstanceOf[C]
  }
}
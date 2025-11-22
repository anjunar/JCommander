package com.anjunar.javafx.dsl

class ComponentBuilder[C] extends ElementBuilder[C] {
  var children: List[ElementBuilder[?]] = Nil

  def add(child: ElementBuilder[?]): Unit =
    children = children :+ child

  override def build(): C = {
    children.head.build().asInstanceOf[C]
  }
}
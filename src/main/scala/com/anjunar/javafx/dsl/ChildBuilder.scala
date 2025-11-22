package com.anjunar.javafx.dsl

import javafx.scene.Node

trait ChildBuilder[C <: Node] extends NodeBuilder[C] {
  def add(child: ElementBuilder[?]): Unit
}

object ChildBuilder {

  def register(child: ElementBuilder[?])
              (using parent: ChildBuilder[?]): Unit =
    parent.add(child)

  def register[C <: ElementBuilder[?]](child: C)
                                          (body: (C, BuildContext) ?=> Unit)
                                          (using parent: ChildBuilder[?], ctx: BuildContext): Unit =
    parent.add(child)
    body(using child, ctx)

}
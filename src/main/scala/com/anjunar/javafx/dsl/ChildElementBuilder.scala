package com.anjunar.javafx.dsl

import javafx.beans.property.SimpleListProperty
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.Node

trait ChildElementBuilder[C, I] extends ElementBuilder[C] {

  var children = new SimpleListProperty[ElementBuilder[?]](FXCollections.observableArrayList[ElementBuilder[?]]())

  def add(child: ElementBuilder[?]): Unit =
    children.add(child)

  def fxObservableList : ObservableList[I]
}

object ChildElementBuilder {

  def register(child: ElementBuilder[?])
              (using parent: ChildElementBuilder[?, ?]): Unit =
    parent.add(child)

  def register[C <: ElementBuilder[?]](child: C)
                                      (body: (C, BuildContext) ?=> Unit)
                                      (using parent: ChildElementBuilder[?, ?], ctx: BuildContext): Unit =
    parent.add(child)
    body(using child, ctx)

}
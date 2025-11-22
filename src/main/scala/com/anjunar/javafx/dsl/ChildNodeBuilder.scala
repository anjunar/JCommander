package com.anjunar.javafx.dsl

import javafx.beans.property.SimpleListProperty
import javafx.beans.value.ObservableValue
import javafx.collections.{FXCollections, ListChangeListener, ObservableList}
import javafx.scene.{Node, layout}

trait ChildNodeBuilder[C <: Node] extends NodeBuilder[C] {

  var children = new SimpleListProperty[ElementBuilder[?]](FXCollections.observableArrayList[ElementBuilder[?]]())

  def add(child: ElementBuilder[?]): Unit =
    children.add(child)

}

object ChildNodeBuilder {

  def register(child: ElementBuilder[?])
              (using parent: ChildNodeBuilder[?]): Unit =
    parent.add(child)

  def register[C <: ElementBuilder[?]](child: C)
                                          (body: (C, BuildContext) ?=> Unit)
                                          (using parent: ChildNodeBuilder[?], ctx: BuildContext): Unit =
    parent.add(child)
    body(using child, ctx)

  def deregister(child: ElementBuilder[?])
                (using parent: ChildNodeBuilder[?]): Unit =
    parent.children.remove(child)

  def react[T](prop: ObservableValue[T])
              (f: T => Unit)
              (using parent: ChildNodeBuilder[?]): Unit =
    f(prop.getValue)
    prop.addListener((_, _, newValue) => f(newValue))

  def react[T <: ElementBuilder[?]](prop: ObservableList[T])
              (using parent: ChildNodeBuilder[?]): Unit =
    prop.addListener(new ListChangeListener[T] {
      override def onChanged(change: ListChangeListener.Change[? <: T]): Unit = {
        while change.next() do
          if change.wasAdded() then
            change.getAddedSubList.forEach(elem =>
              parent.children.add(elem)
            )
          if change.wasRemoved() then
            change.getRemoved.forEach(elem =>
              parent.children.remove(elem)
            )
      }
    })

}
package com.anjunar.javafx.dsl

import javafx.beans.property.SimpleListProperty
import javafx.beans.value.ObservableValue
import javafx.collections.{FXCollections, ListChangeListener, ObservableList}

trait ChildBuilder[C] {

  def children : SimpleListProperty[ElementBuilder[?]]

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

  def deregister(child: ElementBuilder[?])
                (using parent: ChildBuilder[?]): Unit =
    parent.children.remove(child)

  def reactTo[T](prop: ObservableValue[T])
                (f: T => Unit)
                (using parent: ChildBuilder[?]): Unit =
    if (prop.getValue != null) {
      f(prop.getValue)
    }
    prop.addListener((_, _, newValue) => f(newValue))

  def reactTo[T <: ElementBuilder[?]](prop: ObservableList[T])
                                     (using parent: ChildBuilder[?]): Unit = {
    prop.forEach(elem => parent.add(elem))
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


}

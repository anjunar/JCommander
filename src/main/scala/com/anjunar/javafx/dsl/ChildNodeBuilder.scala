package com.anjunar.javafx.dsl

import javafx.beans.property.SimpleListProperty
import javafx.beans.value.ObservableValue
import javafx.collections.{FXCollections, ListChangeListener, ObservableList}
import javafx.scene.{Node, layout}

trait ChildNodeBuilder[C <: Node, I] extends NodeBuilder[C], ChildBuilder[C] {

  var children = new SimpleListProperty[ElementBuilder[?]](FXCollections.observableArrayList[ElementBuilder[?]]())

  def add(child: ElementBuilder[?]): Unit =
    children.add(child)

  def fxObservableList : ObservableList[I]

}
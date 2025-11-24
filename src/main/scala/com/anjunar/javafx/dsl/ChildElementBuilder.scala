package com.anjunar.javafx.dsl

import javafx.beans.property.SimpleListProperty
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.Node

trait ChildElementBuilder[C, I] extends ElementBuilder[C], ChildBuilder[C] {

  var children = new SimpleListProperty[ElementBuilder[?]](FXCollections.observableArrayList[ElementBuilder[?]]())

  def add(child: ElementBuilder[?]): Unit =
    children.add(child)

  def fxObservableList : ObservableList[I]
}
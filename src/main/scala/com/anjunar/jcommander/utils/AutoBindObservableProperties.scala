package com.anjunar.jcommander.utils

import com.anjunar.javafx.dsl.ElementBuilder
import javafx.beans.property.{Property, SimpleListProperty}
import javafx.collections.ListChangeListener.Change
import javafx.collections.ObservableList
import javafx.scene.Node

object AutoBindObservableProperties {

  def observeList[E](children: SimpleListProperty[ElementBuilder[?]], list: ObservableList[E], process : ElementBuilder[?] => E): Unit = {
    if (list != null) {
      list.setAll(children.stream().map(elem => process(elem)).toList)
      children.addListener((change: Change[? <: ElementBuilder[?]]) =>
        while change.next() do
          if change.wasAdded() then {
            change.getAddedSubList.forEach(elem =>
              list.add(process(elem))
            )
          }
          if change.wasRemoved() then
            change.getRemoved.forEach(elem =>
              list.remove(elem.build().asInstanceOf[E])
            )
      )
    } else {
      println("List is null")
    }
  }
  
}

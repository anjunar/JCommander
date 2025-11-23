package com.anjunar.jcommander.utils

import com.anjunar.javafx.dsl.ElementBuilder
import com.anjunar.scala.universe.TypeResolver
import javafx.beans.property.{Property, SimpleListProperty}
import javafx.collections.ListChangeListener.Change
import javafx.collections.ObservableList

object AutoBindObservableProperties {

  def observeList[E](children: SimpleListProperty[ElementBuilder[?]], list: ObservableList[E]): Unit = {
    if (list != null) {
      list.setAll(children.stream().map(elem => elem.build().asInstanceOf[E]).toList)
      children.addListener((change: Change[? <: ElementBuilder[?]]) =>
        while change.next() do
          if change.wasAdded() then {
            change.getAddedSubList.forEach(elem =>
              list.add(elem.build().asInstanceOf[E])
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

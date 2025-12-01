package com.anjunar.javafx.dsl.traits

import com.anjunar.javafx.dsl.ElementBuilder
import javafx.collections.ObservableList

import scala.language.reflectiveCalls

trait HasItems[E] {
  
  lazy val node: AnyRef {
    def getItems(): ObservableList[E]
    def setItems(value: ObservableList[E]): Unit  
  }
  
}

object HasItems {

  def items[E](using h: HasItems[E] & ElementBuilder[?]): ObservableList[E] =
    h.read(h.node.getItems())

  def items_=[E](value: ObservableList[E])(using h: HasItems[E] & ElementBuilder[?]): Unit =
    h.write(() => h.node.setItems(value))

}

package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.{HasHeight, HasItems, HasWidth}
import com.anjunar.javafx.dsl.{BuildContext, ChildElementBuilder, DSL, ElementBuilder, NodeBuilder, Ref}
import javafx.collections.ObservableList
import javafx.scene.control.{ListCell, ListView}
import javafx.util.Callback
import javafx.scene.control.MultipleSelectionModel

class listView[E] extends NodeBuilder[ListView[E]], HasItems[E], HasHeight, HasWidth {

  lazy val node: ListView[E] = new ListView[E]()

  override def build(): ListView[E] = node

}

object listView {

  def apply[T](ref: Ref[listView[T]] = Ref[listView[T]]())(body: (listView[T], BuildContext) ?=> Unit)
              (using ctx: BuildContext, parent: ElementBuilder[?]): ListView[T] =
    DSL.create[ListView[T], listView[T]](ref, new listView[T]())(body)
    
  def cellFactory[E](using h: listView[E]): Callback[ListView[E], ListCell[E]] =
    h.read(h.node.getCellFactory)

  def cellFactory_=[E](value : Callback[ListView[E], ListCell[E]])(using h: listView[E]) : Unit =
    h.write(() => h.node.setCellFactory(value))

  def selectionModel[E](using h: listView[E]) : (MultipleSelectionModel[E] => Unit) => Unit =
    (f: MultipleSelectionModel[E] => Unit) => h.write(() => f(h.node.getSelectionModel))

}
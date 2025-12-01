package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.HasItems
import com.anjunar.javafx.dsl.{BuildContext, DSL, ElementBuilder, NodeBuilder, Producer, Ref}
import javafx.collections.ObservableList
import javafx.scene.control.ComboBox
import javafx.scene.control.SingleSelectionModel

class comboBox[E] extends NodeBuilder[ComboBox[E]], HasItems[E] {

  override lazy val node: ComboBox[E] = new ComboBox[E]()

  override def build(): ComboBox[E] = node
  
}

object comboBox  {

  def apply[T](ref : Ref[comboBox[T]] = Ref[comboBox[T]]())(body: (comboBox[T], BuildContext) ?=> Unit)
              (using ctx: BuildContext, parent: ElementBuilder[?]): ComboBox[T] =
    DSL.create[ComboBox[T], comboBox[T]](ref, new comboBox[T]())(body)

  def singleSelectionModel[E](using h: comboBox[E]) : (SingleSelectionModel[E] => Unit) => Unit =
    (f: SingleSelectionModel[E] => Unit) => h.write(() => f(h.node.getSelectionModel))
}

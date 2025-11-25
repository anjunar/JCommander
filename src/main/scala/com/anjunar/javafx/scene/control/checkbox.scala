package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.{HasOnAction, HasPadding, HasStyle, HasText}
import com.anjunar.javafx.dsl.{BuildContext, ElementBuilder, NodeBuilder, Producer}
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.beans.property.BooleanProperty
import javafx.scene.control.{Button, CheckBox}

class checkbox extends NodeBuilder[CheckBox], HasText, HasOnAction, HasPadding {

  lazy val node  : CheckBox = new CheckBox()

  override def build(): CheckBox = node
  
}

object checkbox extends Producer[checkbox, CheckBox] {
  override def createBuilder: checkbox = new checkbox()

  def selected(using cb: checkbox & ElementBuilder[?], ctx : BuildContext): Boolean =
    cb.read(cb.node.isSelected)

  def selected_=(v: Boolean)(using cb: checkbox & ElementBuilder[?], ctx : BuildContext): Unit =
    cb.write(() => cb.node.setSelected(v))

  def allowIndeterminate(using cb: checkbox & ElementBuilder[?], ctx : BuildContext): Boolean =
    cb.read(cb.node.isAllowIndeterminate)

  def allowIndeterminate_=(v: Boolean)(using cb: checkbox & ElementBuilder[?], ctx : BuildContext): Unit =
    cb.write(() => cb.node.setAllowIndeterminate(v))

  def indeterminate(using cb: checkbox & ElementBuilder[?], ctx : BuildContext): Boolean =
    cb.read(cb.node.isIndeterminate)

  def indeterminate_=(v: Boolean)(using cb: checkbox & ElementBuilder[?], ctx : BuildContext): Unit =
    cb.write(() => cb.node.setIndeterminate(v))

  def selectedProperty(using cb: checkbox & ElementBuilder[?], ctx : BuildContext): (BooleanProperty => Unit) => Unit =
    (f : BooleanProperty => Unit) => cb.write(() => f(cb.node.selectedProperty()))

}

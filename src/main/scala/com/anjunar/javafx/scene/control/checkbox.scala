package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.{HasOnAction, HasPadding, HasStyle, HasText}
import com.anjunar.javafx.dsl.{BuildContext, NodeBuilder, Producer}
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.beans.property.BooleanProperty
import javafx.scene.control.{Button, CheckBox}

class checkbox extends NodeBuilder[CheckBox], HasText, HasOnAction, HasPadding {

  lazy val node  : CheckBox = new CheckBox()

  override def build(): CheckBox = node
  
}

object checkbox extends Producer[checkbox, CheckBox] {
  override def createBuilder: checkbox = new checkbox()

  def selected(using cb: checkbox, b : BuildContext): Boolean = cb.node.isSelected

  def selected_=(v: Boolean)(using cb: checkbox, b : BuildContext): Unit = cb.node.setSelected(v)

  def allowIndeterminate(using cb: checkbox, b : BuildContext): Boolean = cb.node.isAllowIndeterminate

  def allowIndeterminate_=(v: Boolean)(using cb: checkbox, b : BuildContext): Unit = cb.node.setAllowIndeterminate(v)

  def indeterminate(using cb: checkbox, b : BuildContext): Boolean = cb.node.isIndeterminate

  def indeterminate_=(v: Boolean)(using cb: checkbox, b : BuildContext): Unit = cb.node.setIndeterminate(v)

  def selectedProperty(using cb: checkbox, b : BuildContext): BooleanProperty = cb.node.selectedProperty()

}

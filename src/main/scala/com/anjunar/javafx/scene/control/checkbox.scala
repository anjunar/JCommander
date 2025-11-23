package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.HasText
import com.anjunar.javafx.dsl.{NodeBuilder, Producer}
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.beans.property.BooleanProperty
import javafx.scene.control.CheckBox

class checkbox extends NodeBuilder[CheckBox], HasText {

  override lazy val node: CheckBox = new CheckBox()

  override def build(): CheckBox = node
  
}

object checkbox extends Producer[checkbox, CheckBox] {
  override def createBuilder: checkbox = new checkbox()
  
  object HasCheckBox {
    def selected(using cb: checkbox): Boolean = cb.node.isSelected
    def selected_=(v: Boolean)(using cb: checkbox): Unit = cb.node.setSelected(v)
    
    def selectedProperty(using cb: checkbox): BooleanProperty = cb.node.selectedProperty()
  }
  
}

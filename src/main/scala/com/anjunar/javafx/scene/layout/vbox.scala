package com.anjunar.javafx.scene.layout

import com.anjunar.javafx.dsl.traits.{HasHeight, HasSpacing, HasWidth}
import com.anjunar.javafx.dsl.{ChildNodeBuilder, ElementBuilder, Producer}
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.scene.layout.VBox
import javafx.scene.{Node, layout}

class vbox extends ChildNodeBuilder[layout.VBox], HasSpacing, HasWidth, HasHeight {
  lazy val node: layout.VBox = {
    val vBox = new VBox()
    AutoBindObservableProperties.bind(this, vBox)
    AutoBindObservableProperties.observeList(children, () => vBox.getChildren)
    vBox
  }

  override def build(): layout.VBox = node

}

object vbox extends Producer[vbox, layout.VBox] {

  override def createBuilder: vbox = new vbox()
  
}

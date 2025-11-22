package com.anjunar.javafx.scene.layout

import com.anjunar.javafx.dsl.traits.{HasHeight, HasSpacing, HasWidth}
import com.anjunar.javafx.dsl.{ChildNodeBuilder, ElementBuilder, Producer}
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.scene.layout.HBox
import javafx.scene.{Node, layout}

class hbox extends ChildNodeBuilder[layout.HBox], HasSpacing, HasWidth, HasHeight {
  lazy val node: layout.HBox = {
    val hBox = new HBox()
    AutoBindObservableProperties.bind(this, hBox)
    AutoBindObservableProperties.observeList(children, () => hBox.getChildren)
    hBox
  }

  override def build(): layout.HBox = node
  
}

object hbox extends Producer[hbox, layout.HBox] {

  override def createBuilder: hbox = new hbox()
}

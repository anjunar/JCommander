package com.anjunar.javafx.scene.layout

import com.anjunar.javafx.dsl.traits.{HasHeight, HasSpacing, HasWidth}
import com.anjunar.javafx.dsl.{ChildNodeBuilder, ElementBuilder, Producer}
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.collections.ObservableList
import javafx.scene.layout.HBox
import javafx.scene.{Node, layout}

class hbox extends ChildNodeBuilder[layout.HBox, Node], HasSpacing, HasWidth, HasHeight {
  lazy val node: layout.HBox = new HBox()

  override def build(): layout.HBox = node

  override def fxObservableList: ObservableList[Node] = node.getChildren
}

object hbox extends Producer[hbox, layout.HBox] {

  override def createBuilder: hbox = new hbox()
}

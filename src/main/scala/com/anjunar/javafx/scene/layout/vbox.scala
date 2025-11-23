package com.anjunar.javafx.scene.layout

import com.anjunar.javafx.dsl.traits.{HasHeight, HasSpacing, HasWidth}
import com.anjunar.javafx.dsl.{ChildNodeBuilder, ElementBuilder, Producer}
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.collections.ObservableList
import javafx.scene.layout.VBox
import javafx.scene.{Node, layout}

class vbox extends ChildNodeBuilder[layout.VBox, Node], HasSpacing, HasWidth, HasHeight {
  lazy val node : layout.VBox = new VBox()

  override def build(): layout.VBox = node

  override def fxObservableList: ObservableList[Node] = node.getChildren
}

object vbox extends Producer[vbox, layout.VBox] {

  override def createBuilder: vbox = new vbox()
  
}

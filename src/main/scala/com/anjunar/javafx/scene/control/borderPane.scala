package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.{ChildElementBuilder, ChildNodeBuilder, ElementBuilder, NodeBuilder, Producer}
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.layout.BorderPane

class borderPane extends ChildElementBuilder[BorderPane, Node] {

  lazy val node : BorderPane = new BorderPane()

  override def build(): BorderPane = node
  
  override def fxObservableList: ObservableList[Node] = node.getChildren
  
}

object borderPane extends Producer[borderPane, BorderPane] {
  override def createBuilder: borderPane = new borderPane()
}

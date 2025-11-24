package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.{ChildElementBuilder, ChildNodeBuilder, Producer}
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.{ContextMenu, MenuItem}

class contextMenu extends ChildElementBuilder[ContextMenu, MenuItem] {

  lazy val node: ContextMenu = new ContextMenu()

  override def fxObservableList: ObservableList[MenuItem] = node.getItems

  override def build(): ContextMenu = node

}

object contextMenu extends Producer[contextMenu, ContextMenu] {
  override def createBuilder: contextMenu = new contextMenu()
}

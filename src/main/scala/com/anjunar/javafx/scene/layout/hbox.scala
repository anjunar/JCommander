package com.anjunar.javafx.scene.layout

import com.anjunar.javafx.dsl.traits.{HasHeight, HasSpacing, HasWidth}
import com.anjunar.javafx.dsl.{BuildContext, ChildBuilder, DSL, ElementBuilder, Producer, Ref}
import javafx.scene.Node
import javafx.scene.layout

class hbox extends ChildBuilder[layout.HBox], HasSpacing, HasWidth, HasHeight {
  val node: layout.HBox = new layout.HBox()

  override def add(child: ElementBuilder[?]): Unit =
    node.getChildren.add(child.build().asInstanceOf[Node])

  override def build(): layout.HBox = node
}

object hbox extends Producer[hbox, layout.HBox] {

  override def createBuilder: hbox = new hbox()
}

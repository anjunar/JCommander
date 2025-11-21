package com.anjunar.javafx.scene.layout

import com.anjunar.javafx.dsl.traits.{HasHeight, HasSpacing, HasWidth}
import com.anjunar.javafx.dsl.{BuildContext, ChildBuilder, DSL, ElementBuilder, Producer, Ref}
import javafx.scene.Node
import javafx.scene.layout

class vbox extends ChildBuilder[layout.VBox], HasSpacing, HasWidth, HasHeight {
  val node: layout.VBox = new layout.VBox()
  export node.{setSpacing, getSpacing, getAlignment, setAlignment, getPrefWidth, setPrefWidth, getMinWidth, setMinWidth, getMaxWidth, setMaxWidth, getPrefHeight, setPrefHeight, getMinHeight, setMinHeight, getMaxHeight, setMaxHeight}

  var children: List[ElementBuilder[?]] = Nil

  override def add(child: ElementBuilder[?]): Unit =
    children = children :+ child

  override def build(): layout.VBox =
    node.getChildren.clear()
    children.foreach(c => node.getChildren.add(c.build().asInstanceOf[Node]))
    node
}

object vbox extends Producer[vbox, layout.VBox] {

  override def createBuilder: vbox = new vbox()
  
}

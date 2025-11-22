package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.{HasHeight, HasLabeled, HasOnAction, HasText, HasWidth}
import com.anjunar.javafx.dsl.{BuildContext, ChildBuilder, DSL, ElementBuilder, Producer, Ref}
import javafx.scene.{Node, control}

class button extends ChildBuilder[control.Button], HasLabeled, HasText, HasOnAction, HasWidth, HasHeight {
  val node: control.Button = new control.Button()

  var children: List[ElementBuilder[?]] = Nil

  override def add(child: ElementBuilder[?]): Unit =
    children = children :+ child

  override def build(): control.Button = node
}

object button extends Producer[button, control.Button]{
  override def createBuilder: button = new button()
}

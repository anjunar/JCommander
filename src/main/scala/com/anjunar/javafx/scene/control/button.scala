package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.*
import com.anjunar.javafx.dsl.{ChildBuilder, ElementBuilder, Producer}
import javafx.scene.control

class button extends ChildBuilder[control.Button], HasLabeled, HasText, HasOnAction, HasWidth, HasHeight {
  lazy val node: control.Button = new control.Button()

  var children: List[ElementBuilder[?]] = Nil

  override def add(child: ElementBuilder[?]): Unit =
    children = children :+ child

  override def build(): control.Button = node
}

object button extends Producer[button, control.Button]{
  override def createBuilder: button = new button()
}

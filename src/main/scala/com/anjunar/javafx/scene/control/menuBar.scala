package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.*
import com.anjunar.javafx.scene.layout.vbox
import javafx.scene.control.MenuBar as JfxMenuBar

class menuBar extends NodeBuilder[JfxMenuBar] {
  lazy val node : JfxMenuBar = new JfxMenuBar()

  def add(child: ElementBuilder[?]): Unit =
    child match
      case m: menu => node.getMenus.add(m.build())
      case _ => ()

  def build(): JfxMenuBar =
    node
}

object menuBar extends Producer[menuBar, JfxMenuBar]{

  override def createBuilder: menuBar = new menuBar()
}
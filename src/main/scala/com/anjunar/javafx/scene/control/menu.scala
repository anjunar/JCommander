package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.{HasLabeled, HasText}
import com.anjunar.javafx.dsl.*
import com.anjunar.javafx.scene.layout.vbox
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.collections.ObservableList
import javafx.scene.control.{MenuItem, Menu as JfxMenu}

class menu extends ChildElementBuilder[JfxMenu, MenuItem], HasText {
  lazy val node  : JfxMenu = new JfxMenu()

  def build(): JfxMenu = node

  override def fxObservableList: ObservableList[MenuItem] = node.getItems
}

object menu extends Producer[menu, JfxMenu]{
  override def createBuilder: menu = new menu()
}
package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.{HasLabeled, HasText}
import com.anjunar.javafx.dsl.*
import com.anjunar.javafx.scene.layout.vbox
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.scene.control.{MenuItem, Menu as JfxMenu}

class menu extends ChildElementBuilder[JfxMenu], HasText {
  lazy val node : JfxMenu = {
    val menu = new JfxMenu()
    AutoBindObservableProperties.bind(this, menu)
    AutoBindObservableProperties.observeList(children, () => menu.getItems)
    menu
  }

  def build(): JfxMenu = node
}

object menu extends Producer[menu, JfxMenu]{
  override def createBuilder: menu = new menu()
}
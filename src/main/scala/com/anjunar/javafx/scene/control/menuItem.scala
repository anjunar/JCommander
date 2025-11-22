package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.{HasOnAction, HasText}
import com.anjunar.javafx.dsl.*
import com.anjunar.javafx.scene.layout.vbox
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.scene.control.MenuItem as JfxMenuItem

class menuItem extends ElementBuilder[JfxMenuItem], HasText, HasOnAction {
  lazy val node : JfxMenuItem = {
    AutoBindObservableProperties.bind(this, new JfxMenuItem())
  }
  def build(): JfxMenuItem = node
}

object menuItem extends Producer[menuItem, JfxMenuItem] {
  override def createBuilder: menuItem = new menuItem()
}
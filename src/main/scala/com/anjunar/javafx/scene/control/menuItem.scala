package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.{HasOnAction, HasStyle, HasText, HasGraphic}
import com.anjunar.javafx.dsl.*
import javafx.scene.control.MenuItem as JfxMenuItem

class menuItem extends ElementBuilder[JfxMenuItem], HasText, HasOnAction, HasGraphic, HasStyle {
  lazy val node  : JfxMenuItem =  new JfxMenuItem()
  
  def build(): JfxMenuItem = node
}

object menuItem extends Producer[menuItem, JfxMenuItem] {
  override def createBuilder: menuItem = new menuItem()
}
package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.{HasOnAction, HasText}
import com.anjunar.javafx.dsl.*
import com.anjunar.javafx.scene.layout.vbox
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.scene.control.MenuItem as JfxMenuItem

class menuItem extends ElementBuilder[JfxMenuItem], HasText, HasOnAction {
  def create() : JfxMenuItem =  new JfxMenuItem()
  
  def build(): JfxMenuItem = node
}

object menuItem extends Producer[menuItem, JfxMenuItem] {
  override def createBuilder: menuItem = new menuItem()
}
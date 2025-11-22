package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.{HasOnAction, HasText}
import com.anjunar.javafx.dsl.{BuildContext, ChildBuilder, DSL, ElementBuilder, Ref}
import com.anjunar.javafx.scene.layout.vbox
import javafx.scene.control.MenuItem as JfxMenuItem

class menuItem extends ElementBuilder[JfxMenuItem], HasText, HasOnAction {
  lazy val node : JfxMenuItem = new JfxMenuItem()
  def build(): JfxMenuItem = node
}

object menuItem {
  def apply(ref : Ref[menuItem] = Ref())(body: (menuItem, BuildContext) ?=> Unit)
           (using ctx: BuildContext, parent: ElementBuilder[?]): JfxMenuItem = {
    val builder = new menuItem

    val result = DSL.create[JfxMenuItem, menuItem](ref, builder)(body)

    parent match
      case p: menu => p.add(builder)
      case _ => ()
      
    result  
  }
}
package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.{HasLabeled, HasText}
import com.anjunar.javafx.dsl.{BuildContext, ChildBuilder, DSL, ElementBuilder, Ref}
import com.anjunar.javafx.scene.layout.vbox
import javafx.scene.control.Menu as JfxMenu

class menu extends ElementBuilder[JfxMenu], HasText {
  val node = new JfxMenu()
  export node.{getText, setText}
  private var items: List[menuItem] = Nil

  def add(child: ElementBuilder[?]): Unit =
    child match
      case mi: menuItem => items = items :+ mi
      case _ => ()

  def build(): JfxMenu =
    items.foreach(i => node.getItems.add(i.build()))
    node
}

object menu {
  def apply(ref : Ref[menu] = Ref())(body: (menu, BuildContext) ?=> Unit)
           (using ctx: BuildContext, parent: ElementBuilder[?]): JfxMenu = {
    val builder = new menu

    val result = DSL.create[JfxMenu, menu](ref, builder)(body)

    parent match
      case p: menuBar => p.add(builder)
      case _ => ()
      
    result  
  }
}
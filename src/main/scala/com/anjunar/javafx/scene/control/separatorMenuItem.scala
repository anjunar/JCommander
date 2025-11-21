package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.{BuildContext, ChildBuilder, ElementBuilder}
import javafx.scene.control.{MenuItem as JfxMenuItem, SeparatorMenuItem as JfxSeparator}

class separatorMenuItem extends ElementBuilder[JfxMenuItem] {
  val node = new JfxSeparator()

  def build(): JfxMenuItem = node
}

object separatorMenuItem {
  def apply()(using ctx: BuildContext, parent: ElementBuilder[?]): JfxMenuItem =
    val builder = new separatorMenuItem()
    parent match
      case p: ChildBuilder[?] => p.add(builder)
      case _ => ()
    builder.build()
}
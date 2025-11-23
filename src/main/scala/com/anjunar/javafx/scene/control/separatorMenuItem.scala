package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.{BuildContext, ChildNodeBuilder, ElementBuilder, Producer}
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.scene.control.{MenuItem as JfxMenuItem, SeparatorMenuItem as JfxSeparator}

class separatorMenuItem extends ElementBuilder[JfxMenuItem] {
  lazy val node : JfxSeparator = new JfxSeparator()

  def build(): JfxMenuItem = node
}

object separatorMenuItem extends Producer[separatorMenuItem, JfxMenuItem]{
  override def createBuilder: separatorMenuItem = new separatorMenuItem()
}
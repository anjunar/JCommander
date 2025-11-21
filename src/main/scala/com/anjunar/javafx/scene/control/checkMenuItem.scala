package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.{BuildContext, DSL, ElementBuilder, Producer, Ref}
import javafx.scene.control.CheckMenuItem as JfxCheckMenuItem

class checkMenuItem extends ElementBuilder[JfxCheckMenuItem] {
  val node = new JfxCheckMenuItem()

  def build(): JfxCheckMenuItem = node
}


object checkMenuItem extends Producer[checkMenuItem, JfxCheckMenuItem]{
  override def createBuilder: checkMenuItem = new checkMenuItem()
}
    

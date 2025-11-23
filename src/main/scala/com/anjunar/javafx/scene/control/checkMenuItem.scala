package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.*
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.scene.control.CheckMenuItem as JfxCheckMenuItem

class checkMenuItem extends ElementBuilder[JfxCheckMenuItem] {
  lazy val node : JfxCheckMenuItem =  new JfxCheckMenuItem()

  def build(): JfxCheckMenuItem = node
}


object checkMenuItem extends Producer[checkMenuItem, JfxCheckMenuItem]{
  override def createBuilder: checkMenuItem = new checkMenuItem()
}
    

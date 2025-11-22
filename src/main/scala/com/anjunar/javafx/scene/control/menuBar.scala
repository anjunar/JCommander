package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.*
import com.anjunar.javafx.scene.layout.vbox
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.beans.property.SimpleListProperty
import javafx.collections.ListChangeListener.Change
import javafx.collections.ObservableList
import javafx.scene.control.{Menu, MenuBar as JfxMenuBar}

class menuBar extends ChildNodeBuilder[JfxMenuBar] {
  
  lazy val node : JfxMenuBar = {
    val menuBar = new JfxMenuBar()
    AutoBindObservableProperties.bind(this, menuBar)
    AutoBindObservableProperties.observeList(children, () => menuBar.getMenus)
    menuBar
  }

  def build(): JfxMenuBar = node

}

object menuBar extends Producer[menuBar, JfxMenuBar]{

  override def createBuilder: menuBar = new menuBar()
}
package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.*
import com.anjunar.javafx.scene.layout.vbox
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.beans.property.SimpleListProperty
import javafx.collections.ListChangeListener.Change
import javafx.collections.ObservableList
import javafx.scene.control.{Menu, MenuBar as JfxMenuBar}

class menuBar extends ChildNodeBuilder[JfxMenuBar, Menu] {

  lazy val node : JfxMenuBar = new JfxMenuBar()

  def build(): JfxMenuBar = node

  override def fxObservableList: ObservableList[Menu] = node.getMenus
}

object menuBar extends Producer[menuBar, JfxMenuBar]{

  override def createBuilder: menuBar = new menuBar()
}
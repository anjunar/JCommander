package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.{HasGraphic, HasPadding, HasText}
import com.anjunar.javafx.dsl.*
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.scene.control.Button
import javafx.scene.{Node, control}

class label extends NodeBuilder[control.Label], HasGraphic, HasText, HasPadding {

  lazy val node  : control.Label = new control.Label()
  
  override def build(): control.Label = node
}

object label extends Producer[label, control.Label]{

  override def createBuilder: label = new label()
}

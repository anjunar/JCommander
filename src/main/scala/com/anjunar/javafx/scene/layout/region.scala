package com.anjunar.javafx.scene.layout

import com.anjunar.javafx.dsl.*
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.scene.layout

class region extends NodeBuilder[layout.Region] {
  lazy val node : layout.Region = new layout.Region()

  override def build(): layout.Region = node
}

object region extends Producer[region, layout.Region]{

  override def createBuilder: region = new region()
}

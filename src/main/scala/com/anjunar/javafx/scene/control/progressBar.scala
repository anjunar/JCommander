package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.{HasHeight, HasWidth}
import com.anjunar.javafx.dsl.{NodeBuilder, Producer}
import javafx.beans.property.DoubleProperty
import javafx.scene.Node
import javafx.scene.control.ProgressBar

class progressBar extends NodeBuilder[ProgressBar], HasWidth, HasHeight {
  
  override lazy val node: ProgressBar = new ProgressBar()
  
  override def build(): ProgressBar = node
  
}

object progressBar extends Producer[progressBar, ProgressBar] {
  override def createBuilder: progressBar = new progressBar()
  
  object HasProgressBar {
    def progressProperty(using pb: progressBar): DoubleProperty = pb.node.progressProperty()
   }
}

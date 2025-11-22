package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.{ChildNodeBuilder, ElementBuilder, Producer}
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.scene.control.SplitPane
import javafx.scene.{Node, control}

class splitPane extends ChildNodeBuilder[control.SplitPane] {

  lazy val node: control.SplitPane = {
    val splitPane = new SplitPane()
    AutoBindObservableProperties.bind(this, splitPane)
    AutoBindObservableProperties.observeList(children, () => splitPane.getItems)
    splitPane
  }

  override def build(): control.SplitPane = node
  

}

object splitPane extends Producer[splitPane, control.SplitPane] {

  override def createBuilder: splitPane = new splitPane()

  object HasDividerPosition {
    def dividerPositions()(using h: splitPane): Array[Double] = h.node.getDividerPositions()

    def dividerPositions_=(v: Array[Double])(using h: splitPane): Unit = h.node.setDividerPositions(v *)
  }

}

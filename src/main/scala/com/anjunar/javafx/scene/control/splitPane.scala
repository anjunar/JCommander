package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.{ChildNodeBuilder, ElementBuilder, Producer}
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.collections.ObservableList
import javafx.scene.control.SplitPane
import javafx.scene.{Node, control}

class splitPane extends ChildNodeBuilder[control.SplitPane, Node] {

  lazy val node : control.SplitPane = new SplitPane()

  override def build(): control.SplitPane = node

    override def fxObservableList: ObservableList[Node] = node.getItems
}

object splitPane extends Producer[splitPane, control.SplitPane] {

  override def createBuilder: splitPane = new splitPane()

  object HasDividerPosition {
    def dividerPositions()(using h: splitPane): Array[Double] = h.node.getDividerPositions()

    def dividerPositions_=(v: Array[Double])(using h: splitPane): Unit = h.node.setDividerPositions(v *)
  }

}

package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.{BuildContext, ChildNodeBuilder, ElementBuilder, Producer}
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

  def dividerPositions()(using h: splitPane & ElementBuilder[?], ctx : BuildContext): Array[Double] = 
    h.read(h.node.getDividerPositions)

  def dividerPositions_=(v: Array[Double])(using h: splitPane & ElementBuilder[?], ctx : BuildContext): Unit =
    h.write(() => h.node.setDividerPositions(v *))

}

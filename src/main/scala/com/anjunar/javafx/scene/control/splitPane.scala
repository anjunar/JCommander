package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.{ChildBuilder, ElementBuilder, Producer}
import javafx.scene.{Node, control}

class splitPane extends ChildBuilder[control.SplitPane] {

  lazy val node: control.SplitPane = new control.SplitPane()

  override def add(child: ElementBuilder[?]): Unit = node.getItems.add(child.build().asInstanceOf[Node])

  override def build(): control.SplitPane = node

}

object splitPane extends Producer[splitPane, control.SplitPane] {

  override def createBuilder: splitPane = new splitPane()

  object HasDividerPosition {
    def dividerPositions()(using h: splitPane): Array[Double] = h.node.getDividerPositions()

    def dividerPositions_=(v: Array[Double])(using h: splitPane): Unit = h.node.setDividerPositions(v *)
  }

}

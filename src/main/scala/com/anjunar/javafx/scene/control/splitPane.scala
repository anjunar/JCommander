package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.{BuildContext, ChildBuilder, DSL, ElementBuilder, NodeBuilder, Producer, Ref}
import com.anjunar.javafx.scene.control.splitPane.HasDividerPosition
import javafx.scene.{Node, control}

class splitPane extends ChildBuilder[control.SplitPane], HasDividerPosition {

  val node : control.SplitPane = new control.SplitPane()
  export node.{getDividerPositions, setDividerPositions}

  override def add(child: ElementBuilder[?]): Unit = node.getItems.add(child.build().asInstanceOf[Node])

  override def build(): control.SplitPane = node

}

object splitPane extends Producer[splitPane, control.SplitPane]{

  override def createBuilder: splitPane = new splitPane()

  trait HasDividerPosition {
    def getDividerPositions(): Array[Double]
    def setDividerPositions(v: Double*): Unit
  }

  object HasDividerPosition {
    def dividerPositions()(using h: HasDividerPosition): Array[Double] = h.getDividerPositions()
    def dividerPositions_=(v: Array[Double])(using h: HasDividerPosition): Unit = h.setDividerPositions(v*)
  }

  export HasDividerPosition.*

}

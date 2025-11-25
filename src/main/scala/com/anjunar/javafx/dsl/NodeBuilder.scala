package com.anjunar.javafx.dsl

import com.anjunar.javafx.dsl.traits.{HasEventHandler, HasStyle, IsNode}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.geometry.Bounds
import javafx.scene.Node

import scala.collection.mutable.ListBuffer

trait NodeBuilder[N <: Node] extends ElementBuilder[N], HasStyle, HasEventHandler, IsNode {

  private[dsl] val afterLayoutHooks: ListBuffer[() => Unit] = ListBuffer()

  def afterLayout(op: => Unit): Unit =
    afterLayoutHooks += (() => op)

  private[dsl] def runAfterLayout(): Unit =
    afterLayoutHooks.foreach(_())

  def registerLayoutListener(): Unit =
    val listener: javafx.beans.value.ChangeListener[? >: javafx.geometry.Bounds] = new ChangeListener[Bounds] {
      override def changed(observableValue: ObservableValue[_ <: Bounds], t: Bounds, newV: Bounds): Unit = {
        if newV != null && newV.getWidth > 0 && newV.getHeight > 0 then
          lifeCycle = LifeCycle.Layout
          runAfterLayout()
          node.layoutBoundsProperty().removeListener(this)
      }
    }

    node.layoutBoundsProperty().addListener(listener)

}
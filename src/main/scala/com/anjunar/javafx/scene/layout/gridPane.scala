package com.anjunar.javafx.scene.layout

import com.anjunar.javafx.dsl.traits.{HasPadding, HasSpacing, IsNode}
import com.anjunar.javafx.dsl.{BuildContext, ChildNodeBuilder, ElementBuilder, NodeBuilder, Producer}
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.layout.GridPane

class gridPane extends ChildNodeBuilder[GridPane, Node], HasPadding {

  override lazy val node: GridPane = new GridPane()

  override def build(): GridPane = node

  override def add(child: ElementBuilder[?]): Unit = child match {
    case isNode : (IsNode & ElementBuilder[?]) => node.add(isNode.build().asInstanceOf[Node], isNode.gridPaneX, isNode.gridPaneY)
  }

  override def fxObservableList: ObservableList[Node] = null
}

object gridPane extends Producer[gridPane, GridPane] {

  override def createBuilder: gridPane = new gridPane()

  def hgap(using gp: gridPane & ElementBuilder[?], ctx : BuildContext): Double =
    gp.read(gp.node.getHgap)
  def hgap_=(v: Double)(using gp: gridPane & ElementBuilder[?], ctx : BuildContext): Unit =
    gp.write(() => gp.node.setHgap(v))
  def vgap(using gp: gridPane & ElementBuilder[?], ctx : BuildContext): Double =
    gp.read(gp.node.getVgap)
  def vgap_=(v: Double)(using gp: gridPane & ElementBuilder[?], ctx : BuildContext): Unit =
    gp.write(() => gp.node.setVgap(v))

}

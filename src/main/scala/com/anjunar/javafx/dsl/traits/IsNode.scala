package com.anjunar.javafx.dsl.traits

import com.anjunar.javafx.dsl.{BuildContext, ElementBuilder}
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.input.{KeyEvent, MouseEvent}
import javafx.scene.layout.{HBox, Priority, VBox}

import scala.compiletime.uninitialized
import scala.language.reflectiveCalls

trait IsNode {

  lazy val node: Node

  var gridPaneX: Int = uninitialized
  var gridPaneY: Int = uninitialized

  var borderPaneAlignment: Pos = uninitialized

}

object IsNode {

  def position(using h: IsNode & ElementBuilder[?], ctx: BuildContext): Pos =
    h.read(h.borderPaneAlignment)

  def position_=(v: Pos)(using h: IsNode & ElementBuilder[?], ctx: BuildContext): Unit =
    h.write(() => h.borderPaneAlignment = v)

  def gridX(using h: IsNode & ElementBuilder[?], ctx: BuildContext): Int =
    h.read(h.gridPaneX)

  def gridX_=(v: Int)(using h: IsNode & ElementBuilder[?], ctx: BuildContext): Unit =
    h.write(() => h.gridPaneX = v)

  def gridY(using h: IsNode & ElementBuilder[?], ctx: BuildContext): Int =
    h.read(h.gridPaneY)

  def gridY_=(v: Int)(using h: IsNode & ElementBuilder[?], ctx: BuildContext): Unit =
    h.write(() => h.gridPaneY = v)

  def vgrow(using h: IsNode & ElementBuilder[?], ctx: BuildContext): Priority =
    h.read(VBox.getVgrow(h.node))

  def vgrow_=(v: Priority)(using h: IsNode & ElementBuilder[?], ctx: BuildContext): Unit =
    h.write(() => VBox.setVgrow(h.node, v))

  def hgrow(using h: IsNode & ElementBuilder[?], ctx: BuildContext): Priority =
    h.read(HBox.getHgrow(h.node))

  def hgrow_=(v: Priority)(using h: IsNode & ElementBuilder[?], ctx: BuildContext): Unit =
    h.write(() => HBox.setHgrow(h.node, v))


  def onKeyPressed(using h: IsNode & ElementBuilder[?], ctx: BuildContext): EventHandler[? >: KeyEvent] =
    h.read(h.node.getOnKeyPressed())

  def onKeyPressed_=(f: EventHandler[? >: KeyEvent])(using h: IsNode & ElementBuilder[?], ctx: BuildContext): Unit = {
    h.write(() => h.node.setOnKeyPressed(f))
  }


  def onMouseClicked(using h: IsNode & ElementBuilder[?], ctx: BuildContext): EventHandler[? >: MouseEvent] =
    h.read(h.node.getOnMouseClicked())

  def onMouseClicked_=(f: EventHandler[? >: MouseEvent])(using h: IsNode & ElementBuilder[?], ctx: BuildContext): Unit = {
    h.write(() => h.node.setOnMouseClicked(f))
  }

  def onMouseDragged(using h: IsNode & ElementBuilder[?], ctx: BuildContext): EventHandler[? >: MouseEvent] =
    h.read(h.node.getOnMouseDragged())

  def onMouseDragged_=(f: EventHandler[? >: MouseEvent])(using h: IsNode & ElementBuilder[?], ctx: BuildContext): Unit = {
    h.write(() => h.node.setOnMouseDragged(f))
  }

  def onMousePressed(using h: IsNode & ElementBuilder[?], ctx: BuildContext): EventHandler[? >: MouseEvent] =
    h.read(h.node.getOnMousePressed())

  def onMousePressed_=(f: EventHandler[? >: MouseEvent])(using h: IsNode & ElementBuilder[?], ctx: BuildContext): Unit = {
    h.write(() => h.node.setOnMousePressed(f))
  }

}

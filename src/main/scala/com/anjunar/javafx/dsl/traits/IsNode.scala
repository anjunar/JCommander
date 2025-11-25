package com.anjunar.javafx.dsl.traits

import com.anjunar.javafx.dsl.{BuildContext, ElementBuilder}
import javafx.beans.property.{BooleanProperty, SimpleBooleanProperty, SimpleListProperty, SimpleObjectProperty, SimpleStringProperty, StringProperty}
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.{Event, EventHandler, EventType}
import javafx.scene.Node
import javafx.scene.input.{KeyEvent, MouseEvent}
import javafx.scene.layout.{HBox, Priority, VBox}

import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.language.reflectiveCalls

trait IsNode {

  lazy val node : Node
  
  var gridPaneX : Int = uninitialized
  var gridPaneY : Int = uninitialized

}

object IsNode {
  
  def gridX()(using h: IsNode & ElementBuilder[?], ctx : BuildContext): Int = h.gridPaneX
  def gridX_=(v: Int)(using h: IsNode & ElementBuilder[?], ctx : BuildContext): Unit = h.gridPaneX = v
  
  def gridY()(using h: IsNode & ElementBuilder[?], ctx : BuildContext): Int = h.gridPaneY
  def gridY_=(v: Int)(using h: IsNode & ElementBuilder[?], ctx : BuildContext): Unit = h.gridPaneY = v
  
  def vgrow()(using h: IsNode & ElementBuilder[?], ctx : BuildContext): Priority = VBox.getVgrow(h.node)
  def vgrow_=(v: Priority)(using h: IsNode & ElementBuilder[?], ctx : BuildContext): Unit = VBox.setVgrow(h.node, v)

  def hgrow()(using h: IsNode & ElementBuilder[?], ctx : BuildContext): Priority = HBox.getHgrow(h.node)
  def hgrow_=(v: Priority)(using h: IsNode & ElementBuilder[?], ctx : BuildContext): Unit = HBox.setHgrow(h.node, v)

  
  def onKeyPressed(using h: IsNode & ElementBuilder[?], ctx : BuildContext): EventHandler[? >: KeyEvent] = h.node.getOnKeyPressed()

  def onKeyPressed_=(f: EventHandler[? >: KeyEvent])(using h: IsNode & ElementBuilder[?], ctx : BuildContext): Unit = {
    h.node.setOnKeyPressed(f)
  }
  
  
  

  def onMouseClicked(using h: IsNode & ElementBuilder[?], ctx : BuildContext): EventHandler[? >: MouseEvent] =
    h.node.getOnMouseClicked()

  def onMouseClicked_=(f: EventHandler[? >: MouseEvent])(using h: IsNode & ElementBuilder[?], ctx : BuildContext): Unit = {
    h.node.setOnMouseClicked(f)
  }

  def onMouseDragged(using h: IsNode & ElementBuilder[?], ctx : BuildContext): EventHandler[? >: MouseEvent] =
    h.node.getOnMouseDragged()

  def onMouseDragged_=(f: EventHandler[? >: MouseEvent])(using h: IsNode & ElementBuilder[?], ctx : BuildContext): Unit = {
    h.node.setOnMouseDragged(f)
  }

  def onMousePressed(using h: IsNode & ElementBuilder[?], ctx : BuildContext): EventHandler[? >: MouseEvent] =
    h.node.getOnMousePressed()

  def onMousePressed_=(f: EventHandler[? >: MouseEvent])(using h: IsNode & ElementBuilder[?], ctx : BuildContext): Unit = {
    h.node.setOnMousePressed(f)
  }

}

package com.anjunar.javafx.dsl.traits

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
  
  def gridX()(using h: IsNode): Int = h.gridPaneX
  def gridX_=(v: Int)(using h: IsNode): Unit = h.gridPaneX = v
  
  def gridY()(using h: IsNode): Int = h.gridPaneY
  def gridY_=(v: Int)(using h: IsNode): Unit = h.gridPaneY = v
  
  def vgrow()(using h: IsNode): Priority = VBox.getVgrow(h.node)
  def vgrow_=(v: Priority)(using h: IsNode): Unit = VBox.setVgrow(h.node, v)

  def hgrow()(using h: IsNode): Priority = HBox.getHgrow(h.node)
  def hgrow_=(v: Priority)(using h: IsNode): Unit = HBox.setHgrow(h.node, v)

  
  def onKeyPressed(using h: IsNode): EventHandler[? >: KeyEvent] = h.node.getOnKeyPressed()

  def onKeyPressed_=(f: EventHandler[? >: KeyEvent])(using h: IsNode): Unit = {
    h.node.setOnKeyPressed(f)
  }
  
  
  

  def onMouseClicked(using h: IsNode): EventHandler[? >: MouseEvent] =
    h.node.getOnMouseClicked()

  def onMouseClicked_=(f: EventHandler[? >: MouseEvent])(using h: IsNode): Unit = {
    h.node.setOnMouseClicked(f)
  }

  def onMouseDragged(using h: IsNode): EventHandler[? >: MouseEvent] =
    h.node.getOnMouseDragged()

  def onMouseDragged_=(f: EventHandler[? >: MouseEvent])(using h: IsNode): Unit = {
    h.node.setOnMouseDragged(f)
  }

  def onMousePressed(using h: IsNode): EventHandler[? >: MouseEvent] =
    h.node.getOnMousePressed()

  def onMousePressed_=(f: EventHandler[? >: MouseEvent])(using h: IsNode): Unit = {
    h.node.setOnMousePressed(f)
  }

}

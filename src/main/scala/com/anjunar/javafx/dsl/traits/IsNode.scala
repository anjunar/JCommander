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

trait IsNode {

  lazy val node : Node

}

object IsNode {

  def css()(using h: IsNode): mutable.Buffer[String] = h.node.getStyleClass.asScala
  def css_=(values : mutable.Buffer[String])(using h: IsNode): Unit = {
    h.node.getStyleClass.clear()
    h.node.getStyleClass.asScala.addAll(values)
  }

  def style()(using h: IsNode): String = h.node.getStyle()
  def style_=(v: String)(using h: IsNode): Unit = h.node.setStyle(v)

  def vgrow()(using h: IsNode): Priority = VBox.getVgrow(h.node)
  def vgrow_=(v: Priority)(using h: IsNode): Unit = VBox.setVgrow(h.node, v)

  def hgrow()(using h: IsNode): Priority = HBox.getHgrow(h.node)
  def hgrow_=(v: Priority)(using h: IsNode): Unit = HBox.setHgrow(h.node, v)

  
  def addEventHandler[T <: Event](eventType: EventType[T], eventHandler: EventHandler[? >: T])(using h: IsNode) : Unit =
    h.node.addEventHandler(eventType, eventHandler)

  def onKeyPressed(using h: IsNode): EventHandler[? >: KeyEvent] = h.node.getOnKeyPressed

  def onKeyPressed_=(f: EventHandler[? >: KeyEvent])(using h: IsNode): Unit = {
    h.node.setOnKeyPressed(f)
  }
  
  
  

  def onMouseClicked(using h: IsNode): EventHandler[? >: MouseEvent] =
    h.node.getOnMouseClicked

  def onMouseClicked_=(f: EventHandler[? >: MouseEvent])(using h: IsNode): Unit = {
    h.node.setOnMouseClicked(f)
  }

  def onMouseDragged(using h: IsNode): EventHandler[? >: MouseEvent] =
    h.node.getOnMouseDragged

  def onMouseDragged_=(f: EventHandler[? >: MouseEvent])(using h: IsNode): Unit = {
    h.node.setOnMouseDragged(f)
  }

  def onMousePressed(using h: IsNode): EventHandler[? >: MouseEvent] =
    h.node.getOnMousePressed

  def onMousePressed_=(f: EventHandler[? >: MouseEvent])(using h: IsNode): Unit = {
    h.node.setOnMousePressed(f)
  }

}

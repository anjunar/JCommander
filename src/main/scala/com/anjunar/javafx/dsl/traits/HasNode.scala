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

trait HasNode {

  lazy val node : Node

}

object HasNode {

  def css()(using h: HasNode): mutable.Buffer[String] = h.node.getStyleClass.asScala
  def css_=(values : mutable.Buffer[String])(using h: HasNode): Unit = {
    h.node.getStyleClass.clear()
    h.node.getStyleClass.asScala.addAll(values)
  }

  def style()(using h: HasNode): String = h.node.getStyle()
  def style_=(v: String)(using h: HasNode): Unit = h.node.setStyle(v)

  def vgrow()(using h: HasNode): Priority = VBox.getVgrow(h.node)
  def vgrow_=(v: Priority)(using h: HasNode): Unit = VBox.setVgrow(h.node, v)

  def hgrow()(using h: HasNode): Priority = HBox.getHgrow(h.node)
  def hgrow_=(v: Priority)(using h: HasNode): Unit = HBox.setHgrow(h.node, v)

  
  def addEventHandler[T <: Event](eventType: EventType[T], eventHandler: EventHandler[? >: T])(using h: HasNode) : Unit =
    h.node.addEventHandler(eventType, eventHandler)

  def onKeyPressed(using h: HasNode): EventHandler[? >: KeyEvent] = h.node.getOnKeyPressed

  def onKeyPressed_=(f: EventHandler[? >: KeyEvent])(using h: HasNode): Unit = {
    h.node.setOnKeyPressed(f)
  }
  
  
  

  def onMouseClicked(using h: HasNode): EventHandler[? >: MouseEvent] =
    h.node.getOnMouseClicked

  def onMouseClicked_=(f: EventHandler[? >: MouseEvent])(using h: HasNode): Unit = {
    h.node.setOnMouseClicked(f)
  }

  def onMouseDragged(using h: HasNode): EventHandler[? >: MouseEvent] =
    h.node.getOnMouseDragged

  def onMouseDragged_=(f: EventHandler[? >: MouseEvent])(using h: HasNode): Unit = {
    h.node.setOnMouseDragged(f)
  }

  def onMousePressed(using h: HasNode): EventHandler[? >: MouseEvent] =
    h.node.getOnMousePressed

  def onMousePressed_=(f: EventHandler[? >: MouseEvent])(using h: HasNode): Unit = {
    h.node.setOnMousePressed(f)
  }

}

package com.anjunar.javafx.dsl.traits

import javafx.event.{Event, EventHandler, EventType}
import javafx.scene.Node
import javafx.scene.input.{KeyEvent, MouseEvent}
import javafx.scene.layout.{HBox, Priority, VBox}

import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*

trait HasNode {

  lazy val node : Node

  var onKeyPressedHandler : KeyEvent => Unit = uninitialized
  
  var onMouseClickedHandler : MouseEvent => Unit = uninitialized

  var onMouseDraggedHandler: MouseEvent => Unit = uninitialized

  var onMousePressedHandler : MouseEvent => Unit = uninitialized

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
  
  def mouseTransparent(using h: HasNode): Boolean = h.node.isMouseTransparent()
  def mouseTransparent_=(v: Boolean)(using h: HasNode): Unit = h.node.setMouseTransparent(v)  

  def pickOnBounds(using h: HasNode): Boolean = h.node.isPickOnBounds()
  def pickOnBounds_=(v: Boolean)(using h: HasNode): Unit = h.node.setPickOnBounds(v)

  def addEventHandler[T <: Event](eventType: EventType[T], eventHandler: EventHandler[? >: T])(using h: HasNode) : Unit = 
    h.node.addEventHandler(eventType, eventHandler)
  
  def onKeyPressed(using h: HasNode): KeyEvent => Unit =
    h.onKeyPressedHandler

  def onKeyPressed_=(f: KeyEvent => Unit)(using h: HasNode): Unit = {
    h.onKeyPressedHandler = f
    h.node.setOnKeyPressed((t: KeyEvent) => f(t))
  }
  
  
  

  def onMouseClicked(using h: HasNode): MouseEvent => Unit =
    h.onMouseClickedHandler

  def onMouseClicked_=(f: MouseEvent => Unit)(using h: HasNode): Unit = {
    h.onMouseClickedHandler = f
    h.node.setOnMouseClicked((t: MouseEvent) => f(t))
  }

  def onMouseDragged(using h: HasNode): MouseEvent => Unit =
    h.onMouseDraggedHandler

  def onMouseDragged_=(f: MouseEvent => Unit)(using h: HasNode): Unit = {
    h.onMouseDraggedHandler = f
    h.node.setOnMouseDragged((t: MouseEvent) => f(t))
  }

  def onMousePressed(using h: HasNode): MouseEvent => Unit =
    h.onMousePressedHandler

  def onMousePressed_=(f: MouseEvent => Unit)(using h: HasNode): Unit = {
    h.onMousePressedHandler = f
    h.node.setOnMousePressed((t: MouseEvent) => f(t))
  }

}

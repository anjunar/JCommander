package com.anjunar.javafx.dsl.traits

import javafx.beans.property.ObjectProperty
import javafx.event.{Event, EventHandler, EventType}
import javafx.scene.Node
import javafx.scene.input.{KeyEvent, MouseEvent}
import javafx.scene.layout.{HBox, Priority, VBox}

import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*

trait HasNode {

  lazy val node : Node

  var onKeyPressed : KeyEvent => Unit = uninitialized
  
  var onMouseClicked : MouseEvent => Unit = uninitialized

  var onMouseDragged: MouseEvent => Unit = uninitialized

  var onMousePressed : MouseEvent => Unit = uninitialized

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
  
  def onKeyPressed(using h: HasNode): KeyEvent => Unit =
    h.onKeyPressed

  def onKeyPressed_=(f: KeyEvent => Unit)(using h: HasNode): Unit = {
    h.onKeyPressed = f
    h.node.setOnKeyPressed((t: KeyEvent) => f(t))
  }
  
  
  

  def onMouseClicked(using h: HasNode): MouseEvent => Unit =
    h.onMouseClicked

  def onMouseClicked_=(f: MouseEvent => Unit)(using h: HasNode): Unit = {
    h.onMouseClicked = f
    h.node.setOnMouseClicked((t: MouseEvent) => f(t))
  }

  def onMouseDragged(using h: HasNode): MouseEvent => Unit =
    h.onMouseDragged

  def onMouseDragged_=(f: MouseEvent => Unit)(using h: HasNode): Unit = {
    h.onMouseDragged = f
    h.node.setOnMouseDragged((t: MouseEvent) => f(t))
  }

  def onMousePressed(using h: HasNode): MouseEvent => Unit =
    h.onMousePressed

  def onMousePressed_=(f: MouseEvent => Unit)(using h: HasNode): Unit = {
    h.onMousePressed = f
    h.node.setOnMousePressed((t: MouseEvent) => f(t))
  }

}

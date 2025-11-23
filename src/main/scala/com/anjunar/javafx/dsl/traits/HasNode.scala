package com.anjunar.javafx.dsl.traits

import javafx.beans.property.{BooleanProperty, SimpleBooleanProperty, SimpleListProperty, SimpleObjectProperty, SimpleStringProperty, StringProperty}
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.{Event, EventHandler, EventType}
import javafx.scene.Node
import javafx.scene.input.{KeyEvent, MouseEvent}
import javafx.scene.layout.{HBox, Priority, VBox}
import javafx.beans.property.ObjectProperty

import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*

trait HasNode {

  lazy val node: Node
  
//  val getStyleClass: ObservableList[String] = SimpleListProperty[String](FXCollections.observableArrayList())
  val styleProperty: StringProperty = new SimpleStringProperty("")
  val mouseTransparentProperty: BooleanProperty = new SimpleBooleanProperty(false)
  val pickOnBoundsProperty: BooleanProperty = new SimpleBooleanProperty(false)
  
  val onKeyPressedProperty: ObjectProperty[EventHandler[KeyEvent]] = new SimpleObjectProperty[EventHandler[KeyEvent]]()
  
  val onMouseClickedProperty: ObjectProperty[EventHandler[MouseEvent]] = new SimpleObjectProperty[EventHandler[MouseEvent]]()
  val onMouseDraggedProperty: ObjectProperty[EventHandler[MouseEvent]] = new SimpleObjectProperty[EventHandler[MouseEvent]]()
  val onMousePressedProperty: ObjectProperty[EventHandler[MouseEvent]] = new SimpleObjectProperty[EventHandler[MouseEvent]]()

}

object HasNode {

  def addEventHandler[T <: Event](eventType: EventType[T], eventHandler: EventHandler[? >: T])(using h: HasNode): Unit =
    h.node.addEventHandler(eventType, eventHandler)

  def vgrow()(using h: HasNode): Priority = VBox.getVgrow(h.node)
  def vgrow_=(v: Priority)(using h: HasNode): Unit = VBox.setVgrow(h.node, v)

  def hgrow()(using h: HasNode): Priority = HBox.getHgrow(h.node)
  def hgrow_=(v: Priority)(using h: HasNode): Unit = HBox.setHgrow(h.node, v)

  
  
  def css()(using h: HasNode): mutable.Buffer[String] = h.node.getStyleClass.asScala
  def css_=(values : mutable.Buffer[String])(using h: HasNode): Unit = {
    h.node.getStyleClass.clear()
    h.node.getStyleClass.asScala.addAll(values)
  }

  def style()(using h: HasNode): String = h.node.styleProperty.get()
  def style_=(v: String)(using h: HasNode): Unit = h.node.styleProperty.set(v)

  def mouseTransparent(using h: HasNode): Boolean = h.mouseTransparentProperty.get()
  def mouseTransparent_=(v: Boolean)(using h: HasNode): Unit = h.mouseTransparentProperty.set(v)  

  def pickOnBounds(using h: HasNode): Boolean = h.pickOnBoundsProperty.get()
  def pickOnBounds_=(v: Boolean)(using h: HasNode): Unit = h.pickOnBoundsProperty.set(v)


  def onKeyPressed(using h: HasNode): EventHandler[KeyEvent] =
    h.onKeyPressedProperty.get()

  def onKeyPressed_=(f: EventHandler[KeyEvent])(using h: HasNode): Unit = {
    h.onKeyPressedProperty.set(f)
  }
  
  
  

  def onMouseClicked(using h: HasNode): EventHandler[MouseEvent] =
    h.onMouseClickedProperty.get()

  def onMouseClicked_=(f: EventHandler[MouseEvent])(using h: HasNode): Unit = {
    h.node.setOnMouseClicked(f)
  }

  def onMouseDragged(using h: HasNode): EventHandler[MouseEvent] =
    h.onMouseDraggedProperty.get()

  def onMouseDragged_=(f: EventHandler[MouseEvent])(using h: HasNode): Unit = {
    h.node.setOnMouseDragged(f)
  }

  def onMousePressed(using h: HasNode): EventHandler[MouseEvent] =
    h.onMousePressedProperty.get()

  def onMousePressed_=(f: EventHandler[MouseEvent])(using h: HasNode): Unit = {
    h.node.setOnMousePressed(f)
  }

}

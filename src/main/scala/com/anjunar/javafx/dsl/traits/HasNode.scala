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

  var styleProperty: Option[StringProperty] = None

  var mouseTransparentProperty: Option[BooleanProperty] = None

  var pickOnBoundsProperty: Option[BooleanProperty] = None

  var onKeyPressedProperty: Option[ObjectProperty[EventHandler[KeyEvent]]] = None

  var onMouseClickedProperty: Option[ObjectProperty[EventHandler[MouseEvent]]] = None

  var onMouseDraggedProperty: Option[ObjectProperty[EventHandler[MouseEvent]]] = None

  var onMousePressedProperty: Option[ObjectProperty[EventHandler[MouseEvent]]] = None
}

object HasNode {

  def addEventHandler[T <: Event](eventType: EventType[T], handler: EventHandler[? >: T])(using h: HasNode): Unit =
    h.node.addEventHandler(eventType, handler)

  // VBox/HBox grow
  def vgrow(using h: HasNode): Priority = VBox.getVgrow(h.node)

  def vgrow_=(v: Priority)(using h: HasNode): Unit = VBox.setVgrow(h.node, v)

  def hgrow(using h: HasNode): Priority = HBox.getHgrow(h.node)

  def hgrow_=(v: Priority)(using h: HasNode): Unit = HBox.setHgrow(h.node, v)


  // CSS
  def css(using h: HasNode) = h.node.getStyleClass.asScala

  def css_=(values: mutable.Buffer[String])(using h: HasNode): Unit =
    val style = h.node.getStyleClass
    style.clear()
    style.asScala.addAll(values)


  // styleProperty
  def style(using h: HasNode): String =
    h.styleProperty.map(_.get).getOrElse("")

  def style_=(v: String)(using h: HasNode): Unit =
    h.styleProperty match
      case Some(p) => p.set(v)
      case None => h.styleProperty = Some(SimpleStringProperty(v))


  // mouseTransparent
  def mouseTransparent(using h: HasNode): Boolean =
    h.mouseTransparentProperty.exists(_.get)

  def mouseTransparent_=(v: Boolean)(using h: HasNode): Unit =
    h.mouseTransparentProperty match
      case Some(p) => p.set(v)
      case None => h.mouseTransparentProperty = Some(SimpleBooleanProperty(v))


  // pickOnBounds
  def pickOnBounds(using h: HasNode): Boolean =
    h.pickOnBoundsProperty.exists(_.get)

  def pickOnBounds_=(v: Boolean)(using h: HasNode): Unit =
    h.pickOnBoundsProperty match
      case Some(p) => p.set(v)
      case None => h.pickOnBoundsProperty = Some(SimpleBooleanProperty(v))


  // Events
  def onKeyPressed(using h: HasNode): Option[EventHandler[KeyEvent]] =
    h.onKeyPressedProperty.map(_.get)

  def onKeyPressed_=(f: EventHandler[KeyEvent])(using h: HasNode): Unit =
    h.onKeyPressedProperty match
      case Some(p) => p.set(f)
      case None =>
        val p = new SimpleObjectProperty[EventHandler[KeyEvent]](f)
        h.onKeyPressedProperty = Some(p)


  def onMouseClicked(using h: HasNode): Option[EventHandler[MouseEvent]] =
    h.onMouseClickedProperty.map(_.get)

  def onMouseClicked_=(f: EventHandler[MouseEvent])(using h: HasNode): Unit =
    h.node.setOnMouseClicked(f)
    h.onMouseClickedProperty = Some(SimpleObjectProperty(f))


  def onMouseDragged(using h: HasNode): Option[EventHandler[MouseEvent]] =
    h.onMouseDraggedProperty.map(_.get)

  def onMouseDragged_=(f: EventHandler[MouseEvent])(using h: HasNode): Unit =
    h.node.setOnMouseDragged(f)
    h.onMouseDraggedProperty = Some(SimpleObjectProperty(f))


  def onMousePressed(using h: HasNode): Option[EventHandler[MouseEvent]] =
    h.onMousePressedProperty.map(_.get)

  def onMousePressed_=(f: EventHandler[MouseEvent])(using h: HasNode): Unit =
    h.node.setOnMousePressed(f)
    h.onMousePressedProperty = Some(SimpleObjectProperty(f))
}

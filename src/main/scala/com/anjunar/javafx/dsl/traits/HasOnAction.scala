package com.anjunar.javafx.dsl.traits

import javafx.beans.property.SimpleObjectProperty
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.Node
import javafx.scene.control.ButtonBase

import scala.beans.BeanProperty
import scala.compiletime.uninitialized
import scala.language.reflectiveCalls

trait HasOnAction {

  var onActionProperty: Option[SimpleObjectProperty[EventHandler[ActionEvent]]] = None

}

object HasOnAction {

  def onAction(using h: HasOnAction): Option[EventHandler[ActionEvent]] =
    h.onActionProperty.map(_.get)

  def onAction_=(f: ActionEvent => Unit)(using h: HasOnAction): Unit =
    h.onActionProperty match
      case Some(p) =>
        p.set(new EventHandler[ActionEvent] {
          override def handle(event: ActionEvent): Unit = f(event)
        })

      case None =>
        h.onActionProperty = Some(
          new SimpleObjectProperty[EventHandler[ActionEvent]](
            new EventHandler[ActionEvent] {
              override def handle(event: ActionEvent): Unit = f(event)
            }
          )
        )
}
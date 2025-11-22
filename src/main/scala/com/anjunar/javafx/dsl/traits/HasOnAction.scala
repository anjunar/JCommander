package com.anjunar.javafx.dsl.traits

import javafx.beans.property.SimpleObjectProperty
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.Node
import javafx.scene.control.ButtonBase

import scala.beans.BeanProperty
import scala.compiletime.uninitialized
import scala.language.reflectiveCalls

trait HasOnAction {
  
  var onActionProperty = new SimpleObjectProperty[EventHandler[ActionEvent]]() 
  
}

object HasOnAction {
  
  def onAction()(using h: HasOnAction): EventHandler[ActionEvent] = h.onActionProperty.get()

  def onAction_=(f: ActionEvent => Unit)(using h: HasOnAction): Unit = {
    h.onActionProperty.set((t: ActionEvent) => f(t))
  }
}
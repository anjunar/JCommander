package com.anjunar.javafx.dsl.traits

import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.control.ButtonBase

import scala.compiletime.uninitialized

trait HasOnAction {
  def getOnAction(): EventHandler[ActionEvent]
  def setOnAction(value: EventHandler[ActionEvent]): Unit
  
  var onAction : ActionEvent => Unit = uninitialized
}

object HasOnAction :
  def onAction()(using h: HasOnAction): ActionEvent => Unit = h.onAction

  def onAction_=(f: ActionEvent => Unit)(using h: HasOnAction): Unit = {
    h.onAction = f
    h.setOnAction((e: ActionEvent) => f(e))
  }
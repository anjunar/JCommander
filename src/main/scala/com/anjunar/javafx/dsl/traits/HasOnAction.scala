package com.anjunar.javafx.dsl.traits

import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.Node
import javafx.scene.control.ButtonBase

import scala.compiletime.uninitialized
import scala.language.reflectiveCalls

trait HasOnAction {
  
  lazy val node : AnyRef
  
  var onAction : ActionEvent => Unit = uninitialized
}

object HasOnAction {
  
  private type H = {
    def getOnAction(): EventHandler[ActionEvent]
    def setOnAction(value: EventHandler[ActionEvent]): Unit
  }

  private inline def w(using h: HasOnAction): H =
    h.node.asInstanceOf[H]

  def onAction()(using h: HasOnAction): ActionEvent => Unit = h.onAction

  def onAction_=(f: ActionEvent => Unit)(using h: HasOnAction): Unit = {
    h.onAction = f
    w.setOnAction((e: ActionEvent) => f(e))
  }
}
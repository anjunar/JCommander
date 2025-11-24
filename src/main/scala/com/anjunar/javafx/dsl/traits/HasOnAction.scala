package com.anjunar.javafx.dsl.traits

import javafx.beans.property.SimpleObjectProperty
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.Node
import javafx.scene.control.ButtonBase

import scala.compiletime.uninitialized
import scala.language.reflectiveCalls

trait HasOnAction {
  
  lazy val node : AnyRef {
    def getOnAction(): EventHandler[ActionEvent]
    def setOnAction(value: EventHandler[ActionEvent]): Unit
  }
  
}

object HasOnAction {
  
  def onAction()(using h: HasOnAction): EventHandler[ActionEvent] = h.node.getOnAction()

  def onAction_=(f: EventHandler[ActionEvent])(using h: HasOnAction): Unit = {
    h.node.setOnAction(f)
  }
}
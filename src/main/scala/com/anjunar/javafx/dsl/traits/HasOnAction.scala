package com.anjunar.javafx.dsl.traits

import com.anjunar.javafx.dsl.LifeCycle.Apply
import com.anjunar.javafx.dsl.{BuildContext, ElementBuilder}
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
  
  def onAction()(using h: HasOnAction & ElementBuilder[?], ctx : BuildContext): EventHandler[ActionEvent] =
    h.read(h.node.getOnAction())

  def onAction_=(f: EventHandler[ActionEvent])(using h: HasOnAction & ElementBuilder[?], ctx : BuildContext): Unit = {
    h.write(() => h.node.setOnAction(f))
  }
}
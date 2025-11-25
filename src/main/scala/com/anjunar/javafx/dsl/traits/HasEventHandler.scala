package com.anjunar.javafx.dsl.traits

import com.anjunar.javafx.dsl.{BuildContext, ElementBuilder}
import javafx.event.{Event, EventHandler, EventType}

import scala.language.reflectiveCalls

trait HasEventHandler {

  lazy val node: AnyRef
}

object HasEventHandler {
  
  private type A = {
    def addEventHandler(eventType: EventType[?], eventHandler: EventHandler[?]): Unit
  }
  
  def addEventHandler[T <: Event](eventType: EventType[T], eventHandler: EventHandler[? >: T])(using h: HasEventHandler & ElementBuilder[?], ctx : BuildContext): Unit =
    h.node.asInstanceOf[A].addEventHandler(eventType, eventHandler)


}

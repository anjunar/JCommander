package com.anjunar.javafx.dsl.traits

import javafx.beans.property.SimpleBooleanProperty

trait HasHeaderButtons {
  var minimizableProperty: Option[SimpleBooleanProperty] = None
  var maximizableProperty: Option[SimpleBooleanProperty] = None
  var closeableProperty: Option[SimpleBooleanProperty]   = Some(SimpleBooleanProperty(true))
}

object HasHeaderButtons {

  def minimizable(using h: HasHeaderButtons): Boolean =
    h.minimizableProperty.exists(_.get)

  def minimizable_=(v: Boolean)(using h: HasHeaderButtons): Unit =
    h.minimizableProperty match {
      case Some(p) => p.set(v)
      case None    => h.minimizableProperty = Some(SimpleBooleanProperty(v))
    }

  def maximizable(using h: HasHeaderButtons): Boolean =
    h.maximizableProperty.exists(_.get)

  def maximizable_=(v: Boolean)(using h: HasHeaderButtons): Unit =
    h.maximizableProperty match {
      case Some(p) => p.set(v)
      case None    => h.maximizableProperty = Some(SimpleBooleanProperty(v))
    }

  def closeable(using h: HasHeaderButtons): Boolean =
    h.closeableProperty.exists(_.get)

  def closeable_=(v: Boolean)(using h: HasHeaderButtons): Unit =
    h.closeableProperty match {
      case Some(p) => p.set(v)
      case None    => h.closeableProperty = Some(SimpleBooleanProperty(v))
    }
}

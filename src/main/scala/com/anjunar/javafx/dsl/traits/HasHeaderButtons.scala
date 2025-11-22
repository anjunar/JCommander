package com.anjunar.javafx.dsl.traits

import javafx.beans.property.SimpleBooleanProperty

trait HasHeaderButtons {

  var minimizableProperty = new SimpleBooleanProperty(false)
  var maximizableProperty = new SimpleBooleanProperty(false)
  var closeableProperty = new SimpleBooleanProperty(true)

}

object HasHeaderButtons {

  def minimizable[T](using h: HasHeaderButtons): Boolean = h.minimizableProperty.get()

  def minimizable_=[T](value: Boolean)(using h: HasHeaderButtons): Unit = h.minimizableProperty.set(value)

  def maximizable[T](using h: HasHeaderButtons): Boolean = h.maximizableProperty.get()

  def maximizable_=[T](value: Boolean)(using h: HasHeaderButtons): Unit = h.maximizableProperty.set(value)

  def closeable[T](using h: HasHeaderButtons): Boolean = h.closeableProperty.get()

  def closeable_=[T](value: Boolean)(using h: HasHeaderButtons): Unit = h.closeableProperty.set(value)


}

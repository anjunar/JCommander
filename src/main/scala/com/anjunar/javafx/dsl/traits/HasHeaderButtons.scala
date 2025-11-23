package com.anjunar.javafx.dsl.traits

import javafx.beans.property.{BooleanProperty, SimpleBooleanProperty}

trait HasHeaderButtons {

  var minimizableProperty: BooleanProperty = new SimpleBooleanProperty(false)
  var maximizableProperty: BooleanProperty = new SimpleBooleanProperty(false)
  var closeableProperty: BooleanProperty = new SimpleBooleanProperty(true)

}

object HasHeaderButtons {

  def minimizableProp[T](using h: HasHeaderButtons): BooleanProperty = h.minimizableProperty
  def maximizableProp[T](using h: HasHeaderButtons): BooleanProperty = h.maximizableProperty
  def closeableProp[T](using h: HasHeaderButtons): BooleanProperty = h.closeableProperty

  def minimizable[T](using h: HasHeaderButtons): Boolean = h.minimizableProperty.get()

  def minimizable_=[T](value: Boolean)(using h: HasHeaderButtons): Unit = h.minimizableProperty.set(value)

  def maximizable[T](using h: HasHeaderButtons): Boolean = h.maximizableProperty.get()

  def maximizable_=[T](value: Boolean)(using h: HasHeaderButtons): Unit = h.maximizableProperty.set(value)

  def closeable[T](using h: HasHeaderButtons): Boolean = h.closeableProperty.get()

  def closeable_=[T](value: Boolean)(using h: HasHeaderButtons): Unit = h.closeableProperty.set(value)


}

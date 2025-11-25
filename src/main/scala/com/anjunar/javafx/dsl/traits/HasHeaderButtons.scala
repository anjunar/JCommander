package com.anjunar.javafx.dsl.traits

import com.anjunar.javafx.dsl.BuildContext
import javafx.beans.property.{BooleanProperty, SimpleBooleanProperty, SimpleStringProperty, StringProperty}

trait HasHeaderButtons {

  var titleProperty: StringProperty = new SimpleStringProperty("")
  
  var minimizableProperty: BooleanProperty = new SimpleBooleanProperty(false)
  var maximizableProperty: BooleanProperty = new SimpleBooleanProperty(false)
  var closeableProperty: BooleanProperty = new SimpleBooleanProperty(true)

}

object HasHeaderButtons {

  def minimizableProp[T](using h: HasHeaderButtons, b : BuildContext): BooleanProperty = h.minimizableProperty
  def maximizableProp[T](using h: HasHeaderButtons, b : BuildContext): BooleanProperty = h.maximizableProperty
  def closeableProp[T](using h: HasHeaderButtons, b : BuildContext): BooleanProperty = h.closeableProperty
  def titleProp[T](using h: HasHeaderButtons, b : BuildContext): StringProperty = h.titleProperty

  def minimizable[T](using h: HasHeaderButtons, b : BuildContext): Boolean = h.minimizableProperty.get()

  def minimizable_=[T](value: Boolean)(using h: HasHeaderButtons, b : BuildContext): Unit = h.minimizableProperty.set(value)

  def maximizable[T](using h: HasHeaderButtons, b : BuildContext): Boolean = h.maximizableProperty.get()

  def maximizable_=[T](value: Boolean)(using h: HasHeaderButtons, b : BuildContext): Unit = h.maximizableProperty.set(value)

  def closeable[T](using h: HasHeaderButtons, b : BuildContext): Boolean = h.closeableProperty.get()

  def closeable_=[T](value: Boolean)(using h: HasHeaderButtons, b : BuildContext): Unit = h.closeableProperty.set(value)


}

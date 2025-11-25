package com.anjunar.javafx.dsl.traits

import com.anjunar.javafx.dsl.{BuildContext, ElementBuilder}
import javafx.beans.property.{BooleanProperty, SimpleBooleanProperty, SimpleStringProperty, StringProperty}

trait HasHeaderButtons {

  var titleProperty: StringProperty = new SimpleStringProperty("")
  
  var minimizableProperty: BooleanProperty = new SimpleBooleanProperty(false)
  var maximizableProperty: BooleanProperty = new SimpleBooleanProperty(false)
  var closeableProperty: BooleanProperty = new SimpleBooleanProperty(true)

}

object HasHeaderButtons {

  def minimizableProp[T](using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): BooleanProperty = h.minimizableProperty
  def maximizableProp[T](using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): BooleanProperty = h.maximizableProperty
  def closeableProp[T](using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): BooleanProperty = h.closeableProperty
  def titleProp[T](using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): StringProperty = h.titleProperty

  def minimizable[T](using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): Boolean = h.minimizableProperty.get()

  def minimizable_=[T](value: Boolean)(using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): Unit = h.minimizableProperty.set(value)

  def maximizable[T](using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): Boolean = h.maximizableProperty.get()

  def maximizable_=[T](value: Boolean)(using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): Unit = h.maximizableProperty.set(value)

  def closeable[T](using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): Boolean = h.closeableProperty.get()

  def closeable_=[T](value: Boolean)(using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): Unit = h.closeableProperty.set(value)


}

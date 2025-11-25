package com.anjunar.javafx.dsl.traits

import com.anjunar.javafx.dsl.{BuildContext, ElementBuilder}
import javafx.beans.property.{BooleanProperty, SimpleBooleanProperty, SimpleStringProperty, StringProperty}

import scala.compiletime.ops.any.==

trait HasHeaderButtons {

  var titleProperty: StringProperty = new SimpleStringProperty("")
  
  var minimizableProperty: BooleanProperty = new SimpleBooleanProperty(false)
  var maximizableProperty: BooleanProperty = new SimpleBooleanProperty(false)
  var closeableProperty: BooleanProperty = new SimpleBooleanProperty(true)

}

object HasHeaderButtons {

  def minimizableProp[T](using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): (BooleanProperty => Unit) => Unit =
    (f : BooleanProperty => Unit) => h.write( () => f(h.minimizableProperty))
  def maximizableProp[T](using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): (BooleanProperty => Unit) => Unit =
    (f : BooleanProperty => Unit) => h.write( () => f(h.maximizableProperty))
  def closeableProp[T](using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): (BooleanProperty => Unit) => Unit =
    (f : BooleanProperty => Unit) => h.write( () => f(h.closeableProperty))
  def titleProp[T](using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): (StringProperty => Unit) => Unit =
    (f : StringProperty => Unit) => h.write( () => f(h.titleProperty))
    

  def minimizable[T](using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): Boolean = 
    h.read(h.minimizableProperty.get())

  def minimizable_=[T](value: Boolean)(using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): Unit =
    h.write(() => h.minimizableProperty.set(value))

  def maximizable[T](using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): Boolean = 
    h.read(h.maximizableProperty.get())

  def maximizable_=[T](value: Boolean)(using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): Unit =
    h.write(() => h.maximizableProperty.set(value))

  def closeable[T](using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): Boolean = 
    h.read(h.closeableProperty.get())

  def closeable_=[T](value: Boolean)(using h: HasHeaderButtons & ElementBuilder[?], ctx : BuildContext): Unit =
    h.write(() => h.closeableProperty.set(value))


}

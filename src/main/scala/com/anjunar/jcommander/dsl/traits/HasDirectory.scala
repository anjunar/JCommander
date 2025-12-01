package com.anjunar.jcommander.dsl.traits

import com.anjunar.javafx.dsl.{BuildContext, ElementBuilder}
import javafx.beans.property.StringProperty

trait HasDirectory {
  
  def directoryProperty : StringProperty

}

object HasDirectory {
  
  def directory()(using l: HasDirectory & ElementBuilder[?], ctx: BuildContext): String =
    l.read(l.directoryProperty.get)

  def directory_=(value: String)(using l: HasDirectory & ElementBuilder[?], ctx: BuildContext): Unit =
    l.write(() => l.directoryProperty.set(value))

  def directoryProp(using l: HasDirectory & ElementBuilder[?], ctx: BuildContext): (StringProperty => Unit) => Unit =
    (f: StringProperty => Unit) => l.write(() => f(l.directoryProperty))

}

package com.anjunar.javafx.dsl.traits

trait HasHeaderButtons {

  var minimizableFlag = false
  var maximizableFlag = false
  var closeableFlag = true

}

object HasHeaderButtons {

  def minimizable[T](using h: HasHeaderButtons): Boolean = h.minimizableFlag

  def minimizable_=[T](value: Boolean)(using h: HasHeaderButtons): Unit = h.minimizableFlag = value

  def maximizable[T](using h: HasHeaderButtons): Boolean = h.maximizableFlag

  def maximizable_=[T](value: Boolean)(using h: HasHeaderButtons): Unit = h.maximizableFlag = value

  def closeable[T](using h: HasHeaderButtons): Boolean = h.closeableFlag

  def closeable_=[T](value: Boolean)(using h: HasHeaderButtons): Unit = h.closeableFlag = value


}

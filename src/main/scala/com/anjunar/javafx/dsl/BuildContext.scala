package com.anjunar.javafx.dsl

import scala.collection.mutable

class BuildContext {

  private val registry = mutable.Map[BuildContext.Key, Any]()

  def register(name: String, x: Any): Unit = registry(BuildContext.Key(name, x.getClass)) = x

  def resolve[T](name: String, clazz : Class[?]): T = registry(BuildContext.Key(name, clazz)).asInstanceOf[T]

}

object BuildContext {
  case class Key(name : String, clazz : Class[?])
}
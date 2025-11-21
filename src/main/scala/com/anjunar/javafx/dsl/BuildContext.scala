package com.anjunar.javafx.dsl

import javafx.scene.Node

import scala.collection.mutable
import scala.compiletime.uninitialized

class BuildContext {

  private var _root: Node = uninitialized

  def setRoot(n: Node): Unit =
    if _root == null then _root = n

  def root: Node =
    _root

  private val registry = mutable.Map[BuildContext.Key, Any]()

  def register(name: String, x: Any): Unit = registry(BuildContext.Key(name, x.getClass)) = x

  def resolve[T](name: String, clazz : Class[?]): T = registry(BuildContext.Key(name, clazz)).asInstanceOf[T]

}

object BuildContext {
  case class Key(name : String, clazz : Class[?])
}
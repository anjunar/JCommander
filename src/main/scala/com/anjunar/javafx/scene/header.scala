package com.anjunar.javafx.scene

import com.anjunar.javafx.dsl.{ElementBuilder, Producer}

import scala.collection.mutable

class header extends ElementBuilder[header] {

  val children = mutable.ArrayBuffer[ElementBuilder[?]]()

  def add(child: ElementBuilder[?]): Unit =
    children.addOne(child)

  override def build(): header = this

}

object header extends Producer[header, header] {
  override def createBuilder: header = new header()
}

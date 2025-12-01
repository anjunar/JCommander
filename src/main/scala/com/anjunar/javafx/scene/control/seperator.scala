package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.{NodeBuilder, Producer}
import javafx.geometry.Orientation
import javafx.scene.control.Separator

class seperator extends NodeBuilder[Separator] {

  override lazy val node: Separator = new Separator()

  override def build(): Separator = node
  
}

object seperator extends Producer[seperator, Separator] {
  override def createBuilder: seperator = new seperator()
  
  def orientation(using s: seperator & NodeBuilder[?]): String = s.read(s.node.getOrientation.toString)
  def orientation_=(value : Orientation)(using s: seperator & NodeBuilder[?]) : Unit = s.write(() => s.node.setOrientation(value))
  
}

package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.{ElementBuilder, Producer}
import javafx.stage.FileChooser

class fileChooser extends ElementBuilder[FileChooser] {

  lazy val node  : FileChooser = new FileChooser()
  
  override def build(): FileChooser = node
  
}

object fileChooser extends Producer[fileChooser, FileChooser] {
  override def createBuilder: fileChooser = new fileChooser()
  
  def title(using s: fileChooser & ElementBuilder[?]): String = s.read(s.node.getTitle)
  def title_=(value : String)(using s: fileChooser & ElementBuilder[?]) : Unit = s.write(() => s.node.setTitle(value))
}

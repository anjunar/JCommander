package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.*
import com.anjunar.javafx.scene.layout.vbox
import com.anjunar.jcommander.configuration.DarkModeConf

import javafx.scene.Node
import javafx.scene.paint.Color
import org.kordamp.ikonli.javafx.FontIcon
import com.anjunar.jcommander.utils.AutoBindObservableProperties

class Icon extends NodeBuilder[FontIcon] {

  val darkMode = DarkModeConf()

  lazy val node : FontIcon = new FontIcon()

  node.setIconColor(if (darkMode.getValue()) Color.WHITE else Color.BLACK)

  darkMode.valueProperty.addListener { (_, _, isDark) =>
    node.setIconColor(if (isDark) Color.WHITE else Color.BLACK)
  }

  override def build(): FontIcon = node
}

object Icon extends Producer[Icon, FontIcon] {

  override def createBuilder: Icon = new Icon()

  def iconLiteral()(using h: Icon & ElementBuilder[?], ctx : BuildContext): String =
    h.read(h.node.getIconLiteral)

  def iconLiteral_=(v: String)(using h: Icon & ElementBuilder[?], ctx : BuildContext): Unit =
    h.write(() => h.node.setIconLiteral(v))

  def iconSize()(using h: Icon & ElementBuilder[?], ctx : BuildContext): Int =
    h.read(h.node.getIconSize)

  def iconSize_=(v: Int)(using h: Icon & ElementBuilder[?], ctx : BuildContext): Unit =
    h.write(() => h.node.setIconSize(v))

}

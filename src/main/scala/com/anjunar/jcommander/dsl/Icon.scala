package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.*
import com.anjunar.javafx.scene.layout.vbox
import com.anjunar.jcommander.configuration.DarkModeConf
import com.anjunar.jcommander.utils.CdiUtils.inject
import javafx.scene.Node
import javafx.scene.paint.Color
import org.kordamp.ikonli.javafx.FontIcon
import com.anjunar.jcommander.utils.AutoBindObservableProperties

class Icon extends NodeBuilder[FontIcon] {

  val darkMode = inject(classOf[DarkModeConf])

  lazy val node : FontIcon = new FontIcon()

  node.setIconColor(if (darkMode.value) Color.WHITE else Color.BLACK)

  darkMode.valueProperty.onChange { (_, _, isDark) =>
    node.setIconColor(if (isDark) Color.WHITE else Color.BLACK)
  }

  override def build(): FontIcon = node
}

object Icon extends Producer[Icon, FontIcon] {

  override def createBuilder: Icon = new Icon()

  def iconLiteral()(using h: Icon): String = h.node.getIconLiteral()

  def iconLiteral_=(v: String)(using h: Icon): Unit = h.node.setIconLiteral(v)

  def iconSize()(using h: Icon): Int = h.node.getIconSize

  def iconSize_=(v: Int)(using h: Icon): Unit = h.node.setIconSize(v)

}

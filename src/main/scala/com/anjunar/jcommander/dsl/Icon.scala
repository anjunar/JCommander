package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.{BuildContext, ChildBuilder, DSL, ElementBuilder, Producer, Ref}
import com.anjunar.javafx.scene.layout.vbox
import com.anjunar.jcommander.configuration.DarkModeConf
import com.anjunar.jcommander.dsl.Icon.HasFontIcon
import com.anjunar.jcommander.utils.CdiUtils.inject
import javafx.scene.Node
import javafx.scene.paint.Color
import org.kordamp.ikonli.javafx.FontIcon

class Icon extends ElementBuilder[FontIcon], HasFontIcon {

  val darkMode = inject(classOf[DarkModeConf])
  val node = new FontIcon()

  node.setIconColor(if (darkMode.value) Color.WHITE else Color.BLACK)

  darkMode.valueProperty.onChange { (_, _, isDark) =>
    node.setIconColor(if (isDark) Color.WHITE else Color.BLACK)
  }

  override def build(): FontIcon = node
}

object Icon extends Producer[Icon, FontIcon] {

  override def createBuilder: Icon = new Icon()

  trait HasFontIcon {
    val node : FontIcon
  }

  object HasFontIcon {
    def iconLiteral()(using h: HasFontIcon): String = h.node.getIconLiteral()
    def iconLiteral_=(v: String)(using h: HasFontIcon): Unit = h.node.setIconLiteral(v)

    def iconSize()(using h: HasFontIcon): Int = h.node.getIconSize
    def iconSize_=(v: Int)(using h: HasFontIcon): Unit = h.node.setIconSize(v)

  }

  export HasFontIcon.*

}

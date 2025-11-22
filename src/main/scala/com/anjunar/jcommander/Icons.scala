package com.anjunar.jcommander

import com.anjunar.jcommander.configuration.DarkModeConf
import com.anjunar.jcommander.utils.CdiUtils.*
import org.kordamp.ikonli.javafx.FontIcon
import scalafx.scene.paint.Color

object Icons {

  def themedIcon(iconName: String, size: Int = 20): FontIcon = {
    
    val darkMode = inject(classOf[DarkModeConf])
    
    val icon = new FontIcon(iconName)
    icon.setIconSize(size)
    icon.setIconColor(if (darkMode.value) Color.White else Color.Black)

    // reaktiv anpassen, wenn das Theme wechselt
    darkMode.valueProperty.onChange { (_, _, isDark) =>
      icon.setIconColor(if (isDark) Color.White else Color.Black)
    }

    icon
  }

}

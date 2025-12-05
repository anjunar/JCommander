package com.anjunar.jcommander

import com.anjunar.jcommander.configuration.DarkModeConf
import com.anjunar.jcommander.utils.CdiUtils.*
import javafx.scene.paint.Color
import org.kordamp.ikonli.javafx.FontIcon

object Icons {

  def themedIcon(iconName: String, size: Int = 20): FontIcon = {
    
    val darkMode = inject(classOf[DarkModeConf])
    
    val icon = new FontIcon(iconName)
    icon.setIconSize(size)
    icon.setIconColor(if (darkMode.value) Color.WHITE else Color.BLACK)

    darkMode.valueProperty.addListener { (_, _, isDark) =>
      icon.setIconColor(if (isDark) Color.WHITE else Color.BLACK)
    }

    icon
  }

}

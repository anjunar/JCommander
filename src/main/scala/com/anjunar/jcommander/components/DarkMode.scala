package com.anjunar.jcommander.components

import com.anjunar.jcommander.configuration.Configuration
import com.anjunar.jcommander.inject
import jakarta.enterprise.context.ApplicationScoped
import scalafx.beans.property.BooleanProperty
import scalafx.scene.Node
import scalafx.scene.control.{Button, Tooltip}

@ApplicationScoped
class DarkMode extends Component[Button] {

  val configuration: Configuration = inject(classOf[Configuration])

  def value : Boolean = valueProperty.value

  val valueProperty = BooleanProperty(configuration.darkMode)

  lazy val node: Button = new Button(if (value) then "Dark Mode" else "Light Mode") {
    tooltip = new Tooltip(if (value) then "Dark Mode" else "Light Mode")
    onAction = _ => {
      valueProperty.value = !valueProperty.value
      configuration.darkMode = valueProperty.value
      println(s"Dark Mode: ${valueProperty.value}")
    }
  }

}

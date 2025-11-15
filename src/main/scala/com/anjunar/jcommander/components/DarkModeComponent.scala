package com.anjunar.jcommander.components

import com.anjunar.jcommander.configuration.{Configuration, DarkModeConf, PrimaryStageConf}
import com.anjunar.jcommander.CdiUtils.*
import jakarta.enterprise.context.ApplicationScoped
import scalafx.beans.property.BooleanProperty
import scalafx.scene.Node
import scalafx.scene.control.{Button, Tooltip}

class DarkModeComponent extends Component[Button] {

  val configuration: DarkModeConf = inject(classOf[DarkModeConf])

  def value : Boolean = valueProperty.value

  val valueProperty = BooleanProperty(configuration.value)

  val node: Button = new Button(if (value) then "Dark Mode" else "Light Mode") {
    tooltip = new Tooltip(if (value) then "Dark Mode" else "Light Mode")
    onAction = _ => {
      valueProperty.value = !valueProperty.value
      configuration.value = valueProperty.value
      configuration.valueProperty.value = valueProperty.value
      println(s"Dark Mode: ${configuration.value}")
    }
  }

}

package com.anjunar.jcommander

import jakarta.enterprise.context.ApplicationScoped
import scalafx.beans.property.BooleanProperty
import scalafx.scene.Node
import scalafx.scene.control.{Button, Tooltip}

@ApplicationScoped
class DarkMode extends Component[Button] {

  def value : Boolean = valueProperty.value

  val valueProperty = BooleanProperty(true)

  override val node: Button = new Button("Dark Mode") {
    tooltip = new Tooltip("Dark Mode")
    onAction = _ => valueProperty.value = !valueProperty.value
  }

}

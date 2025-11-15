package com.anjunar.jcommander.components

import com.anjunar.jcommander.CdiUtils.*
import jakarta.enterprise.context.ApplicationScoped
import scalafx.geometry.Insets
import scalafx.scene.layout.BorderPane

class RootPaneComponent extends Component[BorderPane] {
  
  val actionButtons = new ActionButtonsComponent()
  
  val splitPane = new SplitPaneComponent

  override val node: BorderPane = new BorderPane {
    padding = Insets(1, 1, 1, 1)
    center = splitPane.node
    bottom = actionButtons.node
  }
  
}

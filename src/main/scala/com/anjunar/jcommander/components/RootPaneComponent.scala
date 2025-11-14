package com.anjunar.jcommander.components

import com.anjunar.jcommander.CdiUtils.*
import jakarta.enterprise.context.ApplicationScoped
import scalafx.geometry.Insets
import scalafx.scene.layout.BorderPane

@ApplicationScoped
class RootPaneComponent extends Component[BorderPane] {
  
  val actionButtons = inject(classOf[ActionButtonsComponent])
  
  val splitPane = inject(classOf[SplitPaneComponent])

  override lazy val node: BorderPane = new BorderPane {
    padding = Insets(1, 1, 1, 1)
    center = splitPane.node
    bottom = actionButtons.node
  }
  
}

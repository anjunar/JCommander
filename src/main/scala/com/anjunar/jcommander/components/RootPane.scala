package com.anjunar.jcommander.components

import com.anjunar.jcommander.inject
import jakarta.enterprise.context.ApplicationScoped
import scalafx.scene.layout.BorderPane

@ApplicationScoped
class RootPane extends Component[BorderPane] {
  
  val topBar = inject(classOf[HeaderMenuBar])
  
  val actionButtons = inject(classOf[ActionButtons])
  
  val splitPane = inject(classOf[SplitPaneComponent])

  override lazy val node: BorderPane = new BorderPane {
    top = topBar.node
    center = splitPane.node
    bottom = actionButtons.node
  }
  
}

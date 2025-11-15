package com.anjunar.jcommander.components

import com.anjunar.jcommander.CdiUtils.*
import jakarta.enterprise.context.ApplicationScoped
import scalafx.geometry.Insets
import scalafx.scene.layout.BorderPane

@ApplicationScoped
class RootPaneComponent(newLeftTable : AbstractFileTableComponent => Unit, 
                        newRightTable : AbstractFileTableComponent => Unit) extends Component[BorderPane] {
  
  val actionButtons = inject(classOf[ActionButtonsComponent])
  
  val splitPane = new SplitPaneComponent(newLeftTable, newRightTable)

  override val node: BorderPane = new BorderPane {
    padding = Insets(1, 1, 1, 1)
    center = splitPane.node
    bottom = actionButtons.node
  }
  
}

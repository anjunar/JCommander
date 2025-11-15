package com.anjunar.jcommander.components

import com.anjunar.jcommander.CdiUtils.*
import jakarta.enterprise.context.ApplicationScoped
import scalafx.scene.control.SplitPane

@ApplicationScoped
class SplitPaneComponent(newLeftTable : AbstractFileTableComponent => Unit, 
                         newRightTable : AbstractFileTableComponent => Unit) extends Component[SplitPane] {

  val leftPane = new FilePaneComponent("left", newLeftTable)
  val rightPane = new FilePaneComponent("right", newRightTable)
  
  override val node: SplitPane = new SplitPane {
    items.addAll(leftPane.node, rightPane.node)
    setDividerPosition(0, 0.5)
  }
}

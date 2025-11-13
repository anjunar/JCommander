package com.anjunar.jcommander.components

import com.anjunar.jcommander.inject
import jakarta.enterprise.context.ApplicationScoped
import scalafx.scene.control.SplitPane

@ApplicationScoped
class SplitPaneComponent extends Component[SplitPane] {

  val leftPane = inject(classOf[FilePane.Left])
  val rightPane = inject(classOf[FilePane.Right])

  override lazy val node: SplitPane = new SplitPane {
    items.addAll(leftPane.node, rightPane.node)
    setDividerPosition(0, 0.5)
  }
}

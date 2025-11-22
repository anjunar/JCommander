package com.anjunar.jcommander.components

import com.anjunar.jcommander.utils.CdiUtils.*
import com.anjunar.jcommander.manager.FileTableManager
import jakarta.enterprise.context.ApplicationScoped
import scalafx.scene.control.SplitPane

class SplitPaneComponent extends Component[SplitPane] {

  val fileTableManager: FileTableManager = inject(classOf[FileTableManager])

  val leftPane = new FilePaneComponent("left", newLeftTable => {
//    fileTableManager.loadLeft(newLeftTable)
  })
  val rightPane = new FilePaneComponent("right", newRightTable => {
//    fileTableManager.loadRight(newRightTable)
  })

  override val node: SplitPane = new SplitPane {
    items.addAll(leftPane.node, rightPane.node)
    setDividerPosition(0, 0.5)
  }
}

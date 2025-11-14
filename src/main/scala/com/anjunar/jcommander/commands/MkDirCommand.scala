package com.anjunar.jcommander.commands

import com.anjunar.jcommander.components.ActiveTableComponent
import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.CdiUtils.*
import jakarta.enterprise.context.Dependent

@Dependent
class MkDirCommand extends Command {

  val activeTable: ActiveTableComponent = inject(classOf[ActiveTableComponent])

  val fileUtils: FileUtils = inject(classOf[FileUtils])

  override def canExecute: Boolean = true

  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.mkDir(activeTable.active)
    }
  }
}

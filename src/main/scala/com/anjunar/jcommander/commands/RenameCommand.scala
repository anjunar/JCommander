package com.anjunar.jcommander.commands

import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.CdiUtils.*
import com.anjunar.jcommander.configuration.ActiveTable
import jakarta.enterprise.context.Dependent

@Dependent
class RenameCommand extends Command {

  val activeTable: ActiveTable = inject(classOf[ActiveTable])

  val fileUtils: FileUtils = inject(classOf[FileUtils])

  override def canExecute: Boolean = ! activeTable.active.node.selectionModel.value.getSelectedItem.isUpDir

  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.renameFile(activeTable.active)
    }
  }
}

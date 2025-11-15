package com.anjunar.jcommander.commands

import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.CdiUtils.*
import com.anjunar.jcommander.configuration.ActiveTable

abstract class AbstractFileCommand extends Command {

  val activeTable: ActiveTable = inject(classOf[ActiveTable])

  val fileUtils: FileUtils = inject(classOf[FileUtils])

  override def canExecute: Boolean = {
    activeTable.active.node.selectionModel.value.getSelectedItem != null &&
      !activeTable.active.node.selectionModel.value.getSelectedItem.isUpDir &&
      activeTable.active.directory != activeTable.inActive.directory
  }
  
}

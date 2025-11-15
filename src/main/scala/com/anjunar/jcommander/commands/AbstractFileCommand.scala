package com.anjunar.jcommander.commands

import com.anjunar.jcommander.CdiUtils.*
import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.manager.FileTableManager

abstract class AbstractFileCommand extends Command {

  val fileTableManager = inject(classOf[FileTableManager])

  val fileUtils: FileUtils = inject(classOf[FileUtils])

  override def canExecute: Boolean = {
    fileTableManager.source.node.selectionModel.value.getSelectedItem != null &&
      !fileTableManager.source.node.selectionModel.value.getSelectedItem.isUpDir &&
      fileTableManager.target.directory != fileTableManager.source.directory
  }

}

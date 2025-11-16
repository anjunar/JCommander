package com.anjunar.jcommander.commands

import com.anjunar.jcommander.utils.CdiUtils.*
import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.manager.{FileManager, FileTableManager}

abstract class AbstractFileCommand extends Command {

  val fileTableManager = inject(classOf[FileTableManager])

  val fileUtils: FileManager = inject(classOf[FileManager])

  override def canExecute: Boolean = {
    val isItemSelected = fileTableManager.source.node.selectionModel.value.getSelectedItem != null
    val isFileNotUpDir = !fileTableManager.source.node.selectionModel.value.getSelectedItem.isUpDir
    val isDirectoryDifferent = fileTableManager.target.directory != fileTableManager.source.directory
    
    isItemSelected && isFileNotUpDir && isDirectoryDifferent
  }

}

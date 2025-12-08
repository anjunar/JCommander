package com.anjunar.jcommander.commands

import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.manager.{FileManager, FileManagerProducer, FileTableManager}

abstract class AbstractFileCommand extends Command {

  val fileTableManager = FileTableManager()

  val fileUtils: FileManager = FileManagerProducer.produces()

  override def canExecute: Boolean = {
    val isItemSelected = fileTableManager.source.node.getSelectionModel.getSelectedItem != null
    val isFileNotUpDir = !fileTableManager.source.node.getSelectionModel.getSelectedItem.isUpDir
    val isDirectoryDifferent = fileTableManager.target.directoryProperty.get() != fileTableManager.source.directoryProperty.get()
    
    isItemSelected && isFileNotUpDir && isDirectoryDifferent
  }

}

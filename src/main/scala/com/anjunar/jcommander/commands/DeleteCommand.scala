package com.anjunar.jcommander.commands

import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.manager.{FileManager, FileManagerProducer, FileTableManager}

class DeleteCommand extends Command {

  val fileTableManager = FileTableManager()

  val fileUtils: FileManager = FileManagerProducer.produces()

  override def canExecute: Boolean = ! fileTableManager.source.node.getSelectionModel.getSelectedItem.isUpDir

  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.deleteFiles(fileTableManager.source, fileTableManager.target)
    }
  }
  
}

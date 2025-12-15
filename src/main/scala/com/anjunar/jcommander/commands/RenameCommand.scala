package com.anjunar.jcommander.commands

import com.anjunar.jcommander.manager.{FileManager, FileManagerProducer, FileTableManager}

class RenameCommand extends Command {

  val fileTableManager = FileTableManager()

  val fileUtils: FileManager = FileManagerProducer.produces()

  override def canExecute: Boolean = !fileTableManager.source.node.getSelectionModel.getSelectedItem.isUpDir

  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.renameFile(fileTableManager.source)
    }
  }
}

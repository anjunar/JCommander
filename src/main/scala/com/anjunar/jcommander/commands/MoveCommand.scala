package com.anjunar.jcommander.commands

class MoveCommand extends AbstractFileCommand {

  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.moveFiles(fileTableManager.source, fileTableManager.target)
    }
  }
  
}

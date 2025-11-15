package com.anjunar.jcommander.commands

import jakarta.enterprise.context.Dependent

@Dependent
class MoveCommand extends AbstractFileCommand {

  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.moveFiles(fileTableManager.source, fileTableManager.target)
    }
  }
  
}

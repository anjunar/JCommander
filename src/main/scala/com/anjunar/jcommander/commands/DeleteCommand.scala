package com.anjunar.jcommander.commands

import jakarta.enterprise.context.Dependent

@Dependent
class DeleteCommand extends AbstractFileCommand {

  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.deleteFiles(activeTable.active, activeTable.inActive)
    }
  }
  
}

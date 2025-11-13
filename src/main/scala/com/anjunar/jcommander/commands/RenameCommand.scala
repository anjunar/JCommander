package com.anjunar.jcommander.commands

import jakarta.enterprise.context.Dependent

@Dependent
class RenameCommand extends AbstractFileCommand {

  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.renameFile(activeTable.active)
    }
  }
}

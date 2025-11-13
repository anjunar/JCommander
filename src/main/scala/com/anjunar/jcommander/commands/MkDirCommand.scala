package com.anjunar.jcommander.commands

import jakarta.enterprise.context.Dependent

@Dependent
class MkDirCommand extends AbstractFileCommand {

  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.mkDir(activeTable.active)
    }
  }
}

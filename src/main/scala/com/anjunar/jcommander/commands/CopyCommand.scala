package com.anjunar.jcommander.commands

import com.anjunar.jcommander.components.ActiveTable
import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.inject
import jakarta.enterprise.context.Dependent

@Dependent
class CopyCommand extends AbstractFileCommand {

  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.copyFiles(activeTable.active, activeTable.inActive)
    }
  }
}

package com.anjunar.jcommander.commands

import com.anjunar.jcommander.files.FileUtils

class CopyCommand extends AbstractFileCommand {

  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.copyFiles(fileTableManager.source, fileTableManager.target)
    }
  }
}

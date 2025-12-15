package com.anjunar.jcommander.commands

import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.manager.{FileManager, FileManagerProducer, FileTableManager}

class MkDirCommand extends Command {

  val fileTableManager = FileTableManager()

  val fileUtils: FileManager = FileManagerProducer.produces()

  override def canExecute: Boolean = true

  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.mkDir(fileTableManager.source)
    }
  }
}

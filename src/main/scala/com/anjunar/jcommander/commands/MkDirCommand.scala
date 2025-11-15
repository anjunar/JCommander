package com.anjunar.jcommander.commands

import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.CdiUtils.*
import com.anjunar.jcommander.manager.FileTableManager
import jakarta.enterprise.context.Dependent

@Dependent
class MkDirCommand extends Command {

  val fileTableManager = inject(classOf[FileTableManager])

  val fileUtils: FileUtils = inject(classOf[FileUtils])

  override def canExecute: Boolean = true

  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.mkDir(fileTableManager.source)
    }
  }
}

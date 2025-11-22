package com.anjunar.jcommander.commands

import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.manager.{FileManager, FileTableManager}
import com.anjunar.jcommander.utils.CdiUtils.*
import jakarta.enterprise.context.Dependent

@Dependent
class MkDirCommand extends Command {

  val fileTableManager = inject(classOf[FileTableManager])

  val fileUtils: FileManager = inject(classOf[FileManager])

  override def canExecute: Boolean = true

  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.mkDir(fileTableManager.source)
    }
  }
}

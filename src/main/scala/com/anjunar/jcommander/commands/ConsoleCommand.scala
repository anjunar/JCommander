package com.anjunar.jcommander.commands

import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.CdiUtils.*
import com.anjunar.jcommander.manager.FileTableManager
import jakarta.enterprise.context.Dependent

@Dependent
class ConsoleCommand extends Command {

  val fileUtils: FileUtils = inject(classOf[FileUtils])

  val fileTableManager = inject(classOf[FileTableManager])
  
  override def canExecute: Boolean = true
  
  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.console(fileTableManager.source.directory)
    }
  }
  
}

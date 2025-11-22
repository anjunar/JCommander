package com.anjunar.jcommander.commands

import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.manager.{FileManager, FileTableManager}
import com.anjunar.jcommander.utils.CdiUtils.*
import jakarta.enterprise.context.Dependent

@Dependent
class ConsoleCommand extends Command {

  val fileUtils: FileManager = inject(classOf[FileManager])

  val fileTableManager = inject(classOf[FileTableManager])
  
  override def canExecute: Boolean = true
  
  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.console(fileTableManager.source)
    }
  }
  
}

package com.anjunar.jcommander.commands

import com.anjunar.jcommander.components.ActiveTable
import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.inject
import jakarta.enterprise.context.Dependent

@Dependent
class ConsoleCommand extends Command {

  val fileUtils: FileUtils = inject(classOf[FileUtils])
  
  val activeTable: ActiveTable = inject(classOf[ActiveTable])
  
  override def canExecute: Boolean = true
  
  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.console(activeTable.active.directory)
    }
  }
  
}

package com.anjunar.jcommander.commands

import com.anjunar.jcommander.components.ActiveTableComponent
import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.CdiUtils.*
import jakarta.enterprise.context.Dependent

@Dependent
class ConsoleCommand extends Command {

  val fileUtils: FileUtils = inject(classOf[FileUtils])
  
  val activeTable: ActiveTableComponent = inject(classOf[ActiveTableComponent])
  
  override def canExecute: Boolean = true
  
  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.console(activeTable.active.directory)
    }
  }
  
}

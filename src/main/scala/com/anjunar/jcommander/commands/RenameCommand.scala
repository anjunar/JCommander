package com.anjunar.jcommander.commands

import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.utils.CdiUtils.*
import com.anjunar.jcommander.manager.{FileManager, FileTableManager, WinFileManager}
import jakarta.enterprise.context.Dependent

@Dependent
class RenameCommand extends Command {

  val fileTableManager = inject(classOf[FileTableManager])

  val fileUtils: FileManager = inject(classOf[FileManager])

  override def canExecute: Boolean = ! fileTableManager.source.node.selectionModel.value.getSelectedItem.isUpDir

  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.renameFile(fileTableManager.source)
    }
  }
}

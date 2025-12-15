package com.anjunar.jcommander.commands

import com.anjunar.jcommander.configuration.TextEditorConf
import com.anjunar.jcommander.manager.FileTableManager

import java.io.File

class EditCommand extends AbstractFileCommand {

  val configuration = TextEditorConf()

  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.executeFile(configuration.executable, null,  Seq(fileTableManager.source.node.getSelectionModel.getSelectedItem.file))
    }
  }
  
}

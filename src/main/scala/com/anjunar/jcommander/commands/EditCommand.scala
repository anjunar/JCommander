package com.anjunar.jcommander.commands

import com.anjunar.jcommander.configuration.TextEditorConf
import com.anjunar.jcommander.manager.FileTableManager
import com.anjunar.jcommander.utils.CdiUtils.*
import jakarta.enterprise.context.Dependent

import java.io.File

@Dependent
class EditCommand extends AbstractFileCommand {

  val configuration = inject(classOf[TextEditorConf])

  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.executeFile(configuration.executable, null,  Seq(fileTableManager.source.node.getSelectionModel.getSelectedItem.file))
    }
  }
  
}

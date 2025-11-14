package com.anjunar.jcommander.commands

import com.anjunar.jcommander.configuration.TextEditorConf
import com.anjunar.jcommander.CdiUtils.*
import jakarta.enterprise.context.Dependent

import java.io.File

@Dependent
class EditCommand extends AbstractFileCommand {

  val configuration = inject(classOf[TextEditorConf])

  override def execute(): Unit = {
    if (canExecute) {
      fileUtils.executeFile(new File(configuration.executable), null, Seq(activeTable.active.node.selectionModel.value.getSelectedItem.file.getAbsolutePath))
    }
  }
  
}

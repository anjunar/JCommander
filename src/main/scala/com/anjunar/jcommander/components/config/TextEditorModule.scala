package com.anjunar.jcommander.components.config

import com.anjunar.jcommander.CdiUtils.inject
import com.anjunar.jcommander.configuration.TextEditorConf
import jakarta.enterprise.context.ApplicationScoped
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.stage.FileChooser

@ApplicationScoped
class TextEditorModule extends ConfigModule {

  val textEditor: TextEditorConf = inject(classOf[TextEditorConf])

  override def name: String = "Text Editor"

  override def getView: VBox = new VBox {
    spacing = 10

    val execField = new TextField {
      text = textEditor.executable
      prefWidth = 350
    }

    val browseBtn = new Button("Browse") {
      onAction = _ => {
        val chooser = new FileChooser {
          title = "Choose Text Editor Executable"
        }

        val file = chooser.showOpenDialog(null)
        if (file != null) {
          execField.text = file.getAbsolutePath
          textEditor.executable = file.getAbsolutePath
        }
      }
    }

    val row = new HBox {
      spacing = 8
      children = Seq(execField, browseBtn)
    }

    children.addAll(
      new Label("Executable:"),
      row
    )
  }

}
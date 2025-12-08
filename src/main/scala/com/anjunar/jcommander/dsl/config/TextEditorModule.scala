package com.anjunar.jcommander.dsl.config

import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.Ref
import com.anjunar.javafx.dsl.traits.HasHeight.maxHeight
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasSpacing.spacing
import com.anjunar.javafx.dsl.traits.HasStyle.style
import com.anjunar.javafx.dsl.traits.HasText.text
import com.anjunar.javafx.dsl.traits.HasWidth.{maxWidth, maxWidth_=, prefWidth}
import com.anjunar.javafx.dsl.traits.IsNode.vgrow
import com.anjunar.javafx.scene.control.fileChooser.title
import com.anjunar.javafx.scene.control.{button, fileChooser, label, textField}
import com.anjunar.javafx.scene.layout.{hbox, vbox}
import com.anjunar.jcommander.configuration.TextEditorConf

import javafx.scene.layout.{Priority, Region}
import javafx.stage.FileChooser

class TextEditorModule extends ConfigModule {

  private val textEditor: TextEditorConf = TextEditorConf()

  private val textFieldRef = Ref[textField]()

  override def name: String = "Text Editor"

  override def getView: vbox = vbox.build() {
    spacing = 10

    maxWidth = Region.USE_PREF_SIZE
    maxHeight = Region.USE_PREF_SIZE

    label() {
      text = "Executable:"
    }

    hbox() {
      spacing = 8
      textField(textFieldRef) {
        text = textEditor.executable
        prefWidth = 350
      }

      button() {
        text = "Browse"
        onAction = _ => {

          val chooser = component[FileChooser] {
            fileChooser() {
              title = "Choose Text Editor Executable"
            }
          }

          val file = chooser.showOpenDialog(null)

          if (file != null) {
            textEditor.executable = file.getAbsolutePath
            textFieldRef {
              text = file.getAbsolutePath
            }
          }

        }
      }
    }

  }
}

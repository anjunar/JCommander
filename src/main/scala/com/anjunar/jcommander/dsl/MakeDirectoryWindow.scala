package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasText.text
import com.anjunar.javafx.dsl.traits.IstTextInput.promptText
import com.anjunar.javafx.dsl.{ElementBuilder, Producer, Ref}
import com.anjunar.javafx.scene.control.{button, label, textField}
import com.anjunar.javafx.scene.{header, window}
import com.anjunar.javafx.scene.layout.hbox
import com.anjunar.javafx.scene.window.{close, closeWithResult}
import com.anjunar.javafx.stage.Window

class MakeDirectoryWindow extends ElementBuilder[Window[String]] {

  private val textFieldRef = Ref[textField]()

  lazy val node : Window[String] = component[Window[String]] {
    window[String]() {
      header() {
        label() {
          text = "Create Directory"
        }
      }

      textField(textFieldRef) {
        promptText = "Directory Name"
      }

      hbox() {
        button() {
          text = "OK"
          onAction = _ => {
            closeWithResult(textFieldRef.get.node.getText)
          }
        }
        button() {
          text = "Cancel"
          onAction = _ => {
            close()
          }
        }
      }
    }
  }

  override def build(): Window[String] = node

}

object MakeDirectoryWindow extends Producer[MakeDirectoryWindow, Window[String]] {
  override def createBuilder: MakeDirectoryWindow = new MakeDirectoryWindow()
}
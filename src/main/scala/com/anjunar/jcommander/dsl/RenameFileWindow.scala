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

class RenameFileWindow extends ElementBuilder[Window[String]] {
  
  private val textFieldRef = Ref[textField]()

  lazy val node : Window[String] = component[Window[String]] {
    window[String]() {
      header() {
        label() {
          text = "Rename File"
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

object RenameFileWindow extends Producer[RenameFileWindow, Window[String]] {
  override def createBuilder: RenameFileWindow = new RenameFileWindow
  
  def directoryName(using h: RenameFileWindow) : String = 
    h.read(h.textFieldRef.get.node.getText)
  def directoryName_=(value : String)(using h: RenameFileWindow) : Unit =
    h.write(() => h.textFieldRef.get.node.setText(value))
}

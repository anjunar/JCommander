package com.anjunar.jcommander.dsl.dialog

import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasPadding.padding
import com.anjunar.javafx.dsl.traits.HasSpacing.{alignment, spacing}
import com.anjunar.javafx.dsl.traits.HasText.text
import com.anjunar.javafx.dsl.traits.IstTextInput.promptText
import com.anjunar.javafx.dsl.{ElementBuilder, Producer, Ref}
import com.anjunar.javafx.scene.control.{button, label, textField}
import com.anjunar.javafx.scene.layout.{hbox, vbox}
import com.anjunar.javafx.scene.window.{close, closeWithResult}
import com.anjunar.javafx.scene.{header, window}
import com.anjunar.javafx.stage.Window
import javafx.geometry.{Insets, Pos}

class RenameFileDialog extends ElementBuilder[Window[String]] {
  
  private val textFieldRef = Ref[textField]()

  lazy val node : Window[String] = component[Window[String]] {
    window[String]() {
      header() {
        label() {
          text = "Rename File"
        }
      }

      vbox() {
        spacing = 10
        padding = new Insets(10)

        textField(textFieldRef) {
          promptText = "Directory Name"
        }

        hbox() {

          spacing = 10
          alignment = Pos.CENTER_RIGHT

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
  }

  override def build(): Window[String] = node
}

object RenameFileDialog extends Producer[RenameFileDialog, Window[String]] {
  override def createBuilder: RenameFileDialog = new RenameFileDialog
  
  def directoryName(using h: RenameFileDialog) : String =
    h.read(h.textFieldRef.get.node.getText)
  def directoryName_=(value : String)(using h: RenameFileDialog) : Unit =
    h.write(() => h.textFieldRef.get.node.setText(value))
}

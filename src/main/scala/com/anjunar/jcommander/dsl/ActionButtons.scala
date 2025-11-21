package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.*
import javafx.scene.layout.{HBox, Priority}

class ActionButtons extends NodeBuilder[HBox] {

  override val node: HBox = component[HBox] {
    hbox() {
      maxWidth = Double.MaxValue

      val buttons = Seq(
        button() {
          text = "F1 Help"
        },
          button() {
          text = "F2 Rename"
        },
          button() {
          text = "F3 Edit"
        },
          button() {
          text = "F4 Console"
        },
          button() {
          text = "F5 Copy"
        },
          button() {
          text = "F6 Move"
        },
          button() {
          text = "F7 MkDir"
        },
          button() {
          text = "F8 Delete"
        },
          button() {
          text = "F9 Menu"
        },
          button() {
          text = "F10 Quit"
        },
          button() {
          text = "Dark Mode"
        }

      )

      buttons.foreach(button => {
        button.setMaxWidth(Double.MaxValue)
        HBox.setHgrow(button, Priority.ALWAYS)
      })

    }
  }

  override def build(): HBox = node

}

object ActionButtons extends Producer[ActionButtons, HBox] {

  override def createBuilder: ActionButtons = new ActionButtons

}

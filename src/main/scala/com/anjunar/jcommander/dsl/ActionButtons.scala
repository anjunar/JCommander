package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.*
import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.jcommander.commands.*
import com.anjunar.jcommander.utils.CdiUtils.inject
import javafx.scene.layout.{HBox, Priority}
import com.anjunar.jcommander.utils.AutoBindObservableProperties
class ActionButtons extends NodeBuilder[HBox] {

  def create(): HBox = {
    val actionButtons = component[HBox] {
      hbox() {
        maxWidth = Double.MaxValue

        val buttons = Seq(
          button() {
            text = "F1 Help"
          },
          button() {
            text = "F2 Rename"
            onAction = _ => {
              inject(classOf[RenameCommand]).execute()
            }
          },
          button() {
            text = "F3 Edit"
            onAction = _ => {
              inject(classOf[EditCommand]).execute()
            }
          },
          button() {
            text = "F4 Console"
            onAction = _ => {
              inject(classOf[ConsoleCommand]).execute()
            }

          },
          button() {
            text = "F5 Copy"
            onAction = _ => {
              inject(classOf[CopyCommand]).execute()
            }

          },
          button() {
            text = "F6 Move"
            onAction = _ => {
              inject(classOf[MoveCommand]).execute()
            }

          },
          button() {
            text = "F7 MkDir"
            onAction = _ => {
              inject(classOf[MkDirCommand]).execute()
            }

          },
          button() {
            text = "F8 Delete"
            onAction = _ => {
              inject(classOf[DeleteCommand]).execute()
            }

          },
          button() {
            text = "F9 Menu"
            onAction = _ => {
            }

          },
          button() {
            text = "F10 Quit"
            onAction = _ => {
              inject(classOf[QuitCommand]).execute()
            }
          },
          button() {
            text = "Dark Mode"
            onAction = _ => {

            }
          }

        )

        buttons.foreach(button => {
          button.setMaxWidth(Double.MaxValue)
          HBox.setHgrow(button, Priority.ALWAYS)
        })

      }
    }

    actionButtons
  }

  override def build(): HBox = node

}

object ActionButtons extends Producer[ActionButtons, HBox] {

  override def createBuilder: ActionButtons = new ActionButtons

}

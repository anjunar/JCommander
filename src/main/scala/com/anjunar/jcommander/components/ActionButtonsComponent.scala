package com.anjunar.jcommander.components

import com.anjunar.jcommander.commands.*
import com.anjunar.jcommander.configuration.{DarkModeConf, TextEditorConf}
import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.utils.CdiUtils.*
import com.anjunar.jcommander.manager.FileTableManager
import com.typesafe.scalalogging.Logger
import jakarta.enterprise.context.ApplicationScoped
import javafx.event.EventHandler
import scalafx.scene.control.Button
import scalafx.scene.layout.{HBox, Priority}

class ActionButtonsComponent extends Component[HBox] {

  val log = Logger[ActionButtonsComponent]

  val fileUtils: FileUtils = inject(classOf[FileUtils])

  val fileTableManager = inject(classOf[FileTableManager])

  val editorConfig: TextEditorConf = inject(classOf[TextEditorConf])

  val node = new HBox {
    spacing = 2
    fillHeight = true
    maxWidth = Double.MaxValue

    val buttons = Seq(
      new Button() {
        text = "F1 Help"
        onMouseClicked = _ => {

        }
      },
      new Button() {
        text = "F2 Rename"
        onMouseClicked = _ => {
          inject(classOf[RenameCommand]).execute()
        }
      },
      new Button() {
        text = "F3 Edit"
        onMouseClicked = _ => {
          inject(classOf[EditCommand]).execute()
        }
      },
      new Button() {
        text = "F4 Console"
        onMouseClicked = _ => {
          inject(classOf[ConsoleCommand]).execute()
        }
      },
      new Button() {
        text = "F5 Copy"
        onMouseClicked = _ => {
          inject(classOf[CopyCommand]).execute()
        }
      },
      new Button() {
        text = "F6 Move"
        onMouseClicked = _ => {
          inject(classOf[MoveCommand]).execute()
        }
      },
      new Button() {
        text = "F7 MkDir"
        onMouseClicked = _ => {
          inject(classOf[MkDirCommand]).execute()
        }
      },
      new Button() {
        text = "F8 Delete"
        onMouseClicked = _ => {
          inject(classOf[DeleteCommand]).execute()
        }
      },
      new Button() {
        text = "F9 Menu"
        onMouseClicked = _ => {

        }
      },
      new Button() {
        text = "F10 Quit"
        onMouseClicked = _ => {
          inject(classOf[QuitCommand]).execute()
        }
      },
      new DarkModeComponent().node
    )

    buttons.foreach { b =>
      b.maxWidth = Double.MaxValue
      HBox.setHgrow(b, Priority.Always)
    }

    children = buttons
  }

}

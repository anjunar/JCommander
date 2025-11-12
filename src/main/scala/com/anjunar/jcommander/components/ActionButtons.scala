package com.anjunar.jcommander.components

import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.{Component, inject}
import com.typesafe.scalalogging.Logger
import jakarta.enterprise.context.ApplicationScoped
import javafx.event.EventHandler
import scalafx.scene.control.Button
import scalafx.scene.layout.{HBox, Priority}

@ApplicationScoped
class ActionButtons extends Component[HBox] {

  val log = Logger[ActionButtons]

  val fileUtils = inject(classOf[FileUtils])

  val activeTable = inject(classOf[ActiveTable])

  val toggleTheme = inject(classOf[DarkMode])

  lazy val node = new HBox {
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
          fileUtils.renameFile(activeTable.active)
        }
      },
      new Button() {
        text = "F3 View"
        onMouseClicked = _ => {

        }
      },
      new Button() {
        text = "F4 Edit"
        onMouseClicked = _ => {

        }
      },
      new Button() {
        text = "F5 Copy"
        onMouseClicked = _ => {
          fileUtils.copyFiles(activeTable.active, activeTable.inActive)
        }
      },
      new Button() {
        text = "F6 Move"
        onMouseClicked = _ => {
          fileUtils.moveFiles(activeTable.active, activeTable.inActive)
        }
      },
      new Button() {
        text = "F7 MkDir"
        onMouseClicked = _ => {
          fileUtils.mkDir(activeTable.active)
        }
      },
      new Button() {
        text = "F8 Delete"
        onMouseClicked = _ => {
          fileUtils.deleteFiles(activeTable.active, activeTable.inActive)
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

        }
      },
      toggleTheme.node
    )

    buttons.foreach { b =>
      b.maxWidth = Double.MaxValue
      HBox.setHgrow(b, Priority.Always)
    }

    children = buttons
  }

}

package com.anjunar.jcommander.ui

import com.anjunar.jcommander.utils.CdiUtils.*
import com.anjunar.jcommander.Icons
import com.anjunar.jcommander.components.DarkModeComponent
import com.anjunar.jcommander.configuration.DarkModeConf
import scalafx.Includes.*
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Cursor
import scalafx.scene.control.Label
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{HBox, Priority, Region}
import scalafx.stage.Stage

class MinimalTitleBar(stage: Stage, title: String = "") {

  private val darkMode = inject(classOf[DarkModeConf])
  private val closeIcon = Icons.themedIcon("mdi2w-window-close")

  private var xOffset = 0.0
  private var yOffset = 0.0

  val titleLabel = new Label(title) {
    padding = Insets(0, 0, 0, 10)
  }

  private def createCloseButton(): HBox = {
    val btn = new HBox {
      padding = Insets(6, 12, 6, 12)
      children = closeIcon
    }

    btn.onMouseEntered = _ =>
      btn.style = s"-fx-background-color: ${if (darkMode.value) "#555" else "#bbb"};"
    btn.onMouseExited = _ =>
      btn.style = "-fx-background-color: transparent;"
    btn.onMouseClicked = _ =>
      stage.close()

    darkMode.valueProperty.onChange { (_, _, isDark) =>
      btn.style = s"-fx-background-color: transparent;"
    }

    btn
  }

  private val closeBtn = createCloseButton()

  private val spacer = new Region
  HBox.setHgrow(spacer, Priority.Always)

  val node: HBox = new HBox {
    spacing = 5
    padding = Insets(0)
    alignment = Pos.CenterLeft
    style = s"-fx-background-color: ${if (darkMode.value) "#1e1e1e" else "#eee"};"
    cursor = Cursor.Move

    children = Seq(titleLabel, spacer, closeBtn)

    onMousePressed = (e: MouseEvent) => {
      xOffset = e.getSceneX
      yOffset = e.getSceneY
    }

    onMouseDragged = (e: MouseEvent) => {
      stage.x = e.getScreenX - xOffset
      stage.y = e.getScreenY - yOffset
    }
  }

  darkMode.valueProperty.onChange { (_, _, isDark) =>
    node.style = s"-fx-background-color: ${if (isDark) "#1e1e1e" else "#eee"};"
  }
}
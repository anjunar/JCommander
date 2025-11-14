package com.anjunar.jcommander.components

import com.anjunar.jcommander.{Icons, inject}
import com.anjunar.jcommander.commands.QuitCommand
import scalafx.geometry.Insets
import scalafx.scene.Cursor
import scalafx.scene.control.Label
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{HBox, Priority, Region}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font
import scalafx.stage.Stage
import scalafx.Includes.*

class TitleBar(stage: Stage) {

  val headerMenuBar = inject(classOf[HeaderMenuBar])

  private var xOffset = 0.0
  private var yOffset = 0.0

  private var savedX = 0.0
  private var savedY = 0.0
  private var savedW = 0.0
  private var savedH = 0.0
  private var maximized = false

  private val darkMode = inject(classOf[DarkMode])

  // Icons automatisch theme-farbig
  private val minimizeIcon = Icons.themedIcon("mdi2w-window-minimize", 20)
  private val maximizeIcon = Icons.themedIcon("mdi2w-window-maximize", 20)
  private val restoreIcon  = Icons.themedIcon("mdi2w-window-restore", 20)
  private val closeIcon    = Icons.themedIcon("mdi2w-window-close", 20)

  private def createButton(icon: scalafx.scene.Node, onClick: => Unit): HBox = {
    val btn = new HBox {
      padding = Insets(6, 12, 6, 12)
      children = icon
    }

    btn.onMouseEntered = _ =>
      btn.style = s"-fx-background-color: ${if (darkMode.value) "#555555" else "#bbb"};"
    btn.onMouseExited = _ =>
      btn.style = s"-fx-background-color: transparent;"

    btn.onMouseClicked = _ => onClick

    // Reaktiv auf Theme-Wechsel
    darkMode.valueProperty.onChange { (_, _, isDark) =>
      btn.style = s"-fx-background-color: ${if (isDark) "#3a3a3a" else "#ddd"};"
    }

    btn
  }

  private val minimizeBtn = createButton(minimizeIcon, stage.setIconified(true))
  private val maximizeBtn = createButton(maximizeIcon, toggleMaximize())
  private val closeBtn = createButton(closeIcon, inject(classOf[QuitCommand]).execute())

  private val spacer = new Region
  HBox.setHgrow(spacer, Priority.Always)

  val box = new HBox {
    spacing = 5
    padding = Insets(0)
    style = s"-fx-background-color: ${if (darkMode.value) "#1e1e1e" else "#eee"};"
    children = Seq(headerMenuBar.node, spacer, minimizeBtn, maximizeBtn, closeBtn)
  }

  darkMode.valueProperty.onChange { (_, _, isDark) =>
    box.style = s"-fx-background-color: ${if (isDark) "#1e1e1e" else "#eee"};"
  }

  box.onMousePressed = (e: MouseEvent) => {
    xOffset = e.getSceneX
    yOffset = e.getSceneY
  }

  box.onMouseDragged = (e: MouseEvent) => {
    stage.x = e.getScreenX - xOffset
    stage.y = e.getScreenY - yOffset
  }

  box.onMouseClicked = e => if (e.getClickCount == 2) toggleMaximize()

  box.cursor = Cursor.Move

  val node = box

  private def toggleMaximize(): Unit = {
    if (!maximized) {
      savedX = stage.x.value
      savedY = stage.y.value
      savedW = stage.width.value
      savedH = stage.height.value

      val screen = javafx.stage.Screen.getPrimary.getVisualBounds
      stage.setX(screen.getMinX)
      stage.setY(screen.getMinY)
      stage.setWidth(screen.getWidth)
      stage.setHeight(screen.getHeight)

      maximizeBtn.children.clear()
      maximizeBtn.children.add(restoreIcon)

      maximized = true
    } else {
      stage.setX(savedX)
      stage.setY(savedY)
      stage.setWidth(savedW)
      stage.setHeight(savedH)

      maximizeBtn.children.clear()
      maximizeBtn.children.add(maximizeIcon)

      maximized = false
    }
  }
}

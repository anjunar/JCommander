package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.*
import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.scene.control.button
import com.anjunar.jcommander.commands.QuitCommand
import com.anjunar.jcommander.dsl.Icon.*
import com.anjunar.jcommander.utils.CdiUtils.inject
import javafx.geometry.Pos
import javafx.scene.layout.{HBox, Priority}
import javafx.stage.{Screen, Stage}

import scala.collection.mutable

class MainTitleBar(val stage: Stage) extends ElementBuilder[HBox] {

  private var xOffset = 0.0
  private var yOffset = 0.0

  private var savedX = 0.0
  private var savedY = 0.0
  private var savedW = 0.0
  private var savedH = 0.0
  private var maximized = false

  private val maximizeIconRef = Ref[Icon]()

  private def toggleMaximize(): Unit = {
    if (!maximized) {
      savedX = stage.getX
      savedY = stage.getY
      savedW = stage.getWidth
      savedH = stage.getHeight

      val screen = Screen.getPrimary.getVisualBounds
      stage.setX(screen.getMinX)
      stage.setY(screen.getMinY)
      stage.setWidth(screen.getWidth)
      stage.setHeight(screen.getHeight)

      maximizeIconRef {
        iconLiteral = "mdi2w-window-restore"
      }

      maximized = true
    } else {
      stage.setX(savedX)
      stage.setY(savedY)
      stage.setWidth(savedW)
      stage.setHeight(savedH)

      maximizeIconRef {
        iconLiteral = "mdi2w-window-maximize"
      }

      maximized = false
    }
  }

  val node: HBox = component[HBox] {
    hbox() {
      css = mutable.ListBuffer("main-title-bar")
      alignment = Pos.CENTER
      spacing = 5

      onMousePressed = event => {
        xOffset = event.getSceneX
        yOffset = event.getSceneY
      }

      onMouseDragged = event => {
        stage.setX(event.getScreenX - xOffset)
        stage.setY(event.getScreenY - yOffset)
      }

      onMouseClicked = event => {
        if (event.getClickCount == 2) toggleMaximize()
      }

      MainMenu() {}
      region() {
        hgrow = Priority.ALWAYS
      }
      button() {
        css = mutable.ListBuffer("title-button")
        onAction = event => {
          stage.setIconified(true)
        }
        graphic = Icon() {
          iconSize = 18
          iconLiteral = "mdi2w-window-minimize"
        }
      }
      button() {
        css = mutable.ListBuffer("title-button")
        onAction = event => {
          toggleMaximize()
        }
        graphic = Icon(maximizeIconRef) {
          iconSize = 18
          iconLiteral = "mdi2w-window-maximize"
        }
      }
      button() {
        css = mutable.ListBuffer("title-button")
        onAction = event => {
          inject(classOf[QuitCommand]).execute()
        }
        graphic = Icon() {
          iconSize = 18
          iconLiteral = "mdi2w-window-close"
        }
      }
    }
  }

  override def build(): HBox = node
}

object MainTitleBar {
  def apply(stage: Stage, ref: Ref[MainTitleBar] = Ref())(body: (MainTitleBar, BuildContext) ?=> Unit)
           (using ctx: BuildContext, parent: ElementBuilder[?]): HBox =
    DSL.create[HBox, MainTitleBar](ref, new MainTitleBar(stage))(body)
}

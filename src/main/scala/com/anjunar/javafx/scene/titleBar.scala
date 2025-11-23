package com.anjunar.javafx.scene

import com.anjunar.javafx.dsl.*
import com.anjunar.javafx.dsl.traits.HasHeaderButtons
import com.anjunar.javafx.dsl.traits.IsNode.{css, hgrow, onMouseClicked, onMouseDragged, onMousePressed, style}
import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasSpacing.{alignment, spacing}
import com.anjunar.javafx.dsl.traits.HasText.text
import com.anjunar.javafx.dsl.traits.IsLabeled.graphic
import com.anjunar.javafx.dsl.ChildNodeBuilder.{reactTo, register, deregister}
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import com.anjunar.javafx.scene.control.button
import com.anjunar.javafx.scene.layout.{hbox, region, vbox}
import com.anjunar.jcommander.commands.QuitCommand
import com.anjunar.jcommander.dsl.Icon
import com.anjunar.jcommander.dsl.Icon.IsIcon.*
import com.anjunar.jcommander.utils.CdiUtils.inject
import javafx.beans.property.SimpleListProperty
import javafx.collections.{FXCollections, ObservableList}
import javafx.geometry.Pos
import javafx.scene.layout.{HBox, Priority}
import javafx.stage.{Screen, Stage}

import scala.collection.mutable
import scala.compiletime.uninitialized

class titleBar(val stage: Stage) extends ChildNodeBuilder[HBox, ElementBuilder[?]], HasHeaderButtons {

  private var xOffset = 0.0
  private var yOffset = 0.0

  private var savedX = 0.0
  private var savedY = 0.0
  private var savedW = 0.0
  private var savedH = 0.0
  private var maximized = false

  private val maximizeIconRef = Ref[Icon]()
  private val content = new SimpleListProperty[ElementBuilder[?]](FXCollections.observableArrayList[ElementBuilder[?]]())

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

  lazy val node : HBox = {
    val titleBar = component[HBox] {
      hbox() {
        css = mutable.ListBuffer("main-title-bar")
        alignment = Pos.CENTER
        spacing = 5

        hbox() {
          spacing = 10
          reactTo(content)
        }

        region() {
          hgrow = Priority.ALWAYS

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

        }

        button() {
          text = "TEST"
          onAction = _ => {
            minimizableProperty.set(!minimizableProperty.get())
          }
        }

        val minimizeButton = button.build() {
          css = mutable.ListBuffer("title-button")
          onAction = event => {
            stage.setIconified(true)
          }
          graphic = Icon() {
            iconSize = 18
            iconLiteral = "mdi2w-window-minimize"
          }
        }

        vbox() {
          reactTo(minimizableProperty) { minimizable =>
            if (minimizable) {
              register(minimizeButton)
            } else {
              deregister(minimizeButton)
            }
          }
        }

        val maximizeButton = button.build() {
          css = mutable.ListBuffer("title-button")
          onAction = event => {
            toggleMaximize()
          }
          graphic = Icon(maximizeIconRef) {
            iconSize = 18
            iconLiteral = "mdi2w-window-maximize"
          }
        }

        vbox() {
          reactTo(maximizableProperty) { minimizable =>
            if (minimizable) {
              register(maximizeButton)
            } else {
              deregister(maximizeButton)
            }
          }
        }

        val closeButton = button.build() {
          css = mutable.ListBuffer("title-button")
          onAction = event => {
            stage.close()
          }
          graphic = Icon() {
            iconSize = 18
            iconLiteral = "mdi2w-window-close"
          }
        }

        vbox() {
          reactTo(closeableProperty) { minimizable =>
            if (minimizable) {
              register(closeButton)
            } else {
              deregister(closeButton)
            }
          }
        }


      }
    }

    titleBar
  }

  override def add(child: ElementBuilder[?]): Unit = {
    content.add(child)
  }

  override def build(): HBox = node

  override def fxObservableList: ObservableList[ElementBuilder[?]] = null
}

object titleBar {
  def apply(stage: Stage, ref: Ref[titleBar] = Ref())(body: (titleBar, BuildContext) ?=> Unit)
           (using ctx: BuildContext, parent: ElementBuilder[?]): HBox =
    DSL.create[HBox, titleBar](ref, new titleBar(stage))(body)

}

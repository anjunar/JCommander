package com.anjunar.javafx.scene

import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.traits.HasText
import com.anjunar.javafx.dsl.{BuildContext, DSL, ElementBuilder, Ref}
import com.anjunar.javafx.stage.Window
import com.anjunar.jcommander.configuration.DarkModeConf
import com.anjunar.jcommander.ui.Resizable
import com.anjunar.jcommander.utils.CdiUtils.inject
import javafx.scene.Scene
import javafx.scene.layout.VBox
import javafx.stage.StageStyle

import scala.compiletime.uninitialized
import scala.concurrent.Promise

class window[E](width: Double, height: Double) extends ElementBuilder[Window[E]], HasText {

  private val darkMode: DarkModeConf = inject(classOf[DarkModeConf])

  private val promise = Promise[E]()

  private var content : ElementBuilder[?] = uninitialized

  private var header : header = uninitialized

  lazy val node: Window[E] = {
    val stage = new Window[E]()
    stage.initStyle(StageStyle.UNDECORATED)

    val ui = component[VBox] {
      vbox() {
        style = "-fx-border-color: #444; -fx-border-width: 1;"
        titleBar(stage) {
          if (header != null) header.children.foreach(child => addComponent(child))
        }
        addComponent(content)
      }
    }

    val scene = new Scene(ui, width, height)

    val lightCSS = getClass.getResource("/light-theme.css").toExternalForm
    val darkCSS = getClass.getResource("/dark-theme.css").toExternalForm
    scene.getStylesheets.add(if darkMode.value then darkCSS else lightCSS)

    darkMode.valueProperty.onChange { (_, _, isDark) => {
      val theme = if (darkMode.value) "dark" else "light"
      scene.getStylesheets.clear()
      scene.getStylesheets.add(getClass.getResource(s"/$theme-theme.css").toExternalForm)
    }
    }

    new Resizable(stage, ui)

    stage.setOnCloseRequest { _ =>
      if !promise.isCompleted then
        promise.trySuccess(node.result.get)
    }

    stage.setScene(scene)
    stage
  }

  def add(child: ElementBuilder[?]): Unit = {
    child match
      case h: header =>
        header = h

      case _ =>
        content = child
  }

  def closeWithResult(value: E): Unit =
    node.result = Some(value)
    promise.trySuccess(value)
    node.close()

  override def build(): Window[E] = node

}

object window {

  def apply[T](width: Double = -1, height: Double = -1)(body: (window[T], BuildContext) ?=> Unit)
              (using ctx: BuildContext, parent: ElementBuilder[?]): Window[T] =
    DSL.create[Window[T], window[T]](Ref(), new window[T](width, height))(body)

  object HasWindow {

    def closeWithResult[T](value: T)(using h: window[T]): Unit = h.closeWithResult(value)

    def close[T]()(using h: window[T]): Unit = h.node.close()
    
    def resizable[T](using h: window[T]) : Boolean = h.node.isResizable
    def resizable_=[T](value : Boolean)(using h: window[T]) : Unit = h.node.setResizable(value)

  }

}

package com.anjunar.javafx.scene

import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.traits.HasText
import com.anjunar.javafx.dsl.{BuildContext, DSL, ElementBuilder, Ref}
import com.anjunar.jcommander.configuration.DarkModeConf
import com.anjunar.jcommander.ui.Resizable
import com.anjunar.jcommander.utils.CdiUtils.inject
import javafx.scene.layout.{Priority, VBox}
import javafx.scene.{Node, Scene}
import javafx.stage.StageStyle

import scala.concurrent.Promise

class stage[E](width: Double, height: Double) extends ElementBuilder[Window[E]], HasText {

  private val rootRef = Ref[vbox]()

  private val darkMode: DarkModeConf = inject(classOf[DarkModeConf])

  private val promise = Promise[E]()

  val node: Window[E] = {
    val stage = new Window[E]()
    stage.initStyle(StageStyle.UNDECORATED)

    val ui = component[VBox] {
      vbox(rootRef) {
        style = "-fx-border-color: #444; -fx-border-width: 1;"
        titleBar(stage) {}
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
    val element = child.build().asInstanceOf[Node]
    VBox.setVgrow(element, Priority.ALWAYS)
    rootRef.value.node.getChildren.add(element)
  }

  def closeWithResult(value: E): Unit =
    node.result = Some(value)
    promise.trySuccess(value)
    node.close()

  override def build(): Window[E] = node

}

object stage {

  def apply[T](width: Double = -1, height: Double = -1)(body: (stage[T], BuildContext) ?=> Unit)
              (using ctx: BuildContext, parent: ElementBuilder[?]): Window[T] =
    DSL.create[Window[T], stage[T]](Ref(), new stage[T](width, height))(body)

  object HasStage {

    def closeWithResult[T](value: T)(using h: stage[T]): Unit = h.closeWithResult(value)

    def close[T]()(using h: stage[T]): Unit = h.node.close()
  }

}

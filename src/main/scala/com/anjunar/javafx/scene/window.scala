package com.anjunar.javafx.scene

import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.traits.HasHeaderButtons.{closeableProp, maximizableProp, minimizableProp, titleProp}
import com.anjunar.javafx.dsl.traits.{HasHeaderButtons, HasText}
import com.anjunar.javafx.dsl.{BuildContext, DSL, ElementBuilder, Ref}
import com.anjunar.javafx.dsl.ChildBuilder.{deregister, reactTo, register}
import com.anjunar.javafx.dsl.traits.HasStyle.style
import com.anjunar.javafx.dsl.traits.IsNode.vgrow
import com.anjunar.javafx.scene.layout.vbox
import com.anjunar.javafx.stage.Window
import com.anjunar.jcommander.configuration.DarkModeConf
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import com.anjunar.jcommander.ui.Resizable
import com.anjunar.jcommander.utils.CdiUtils.inject
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Scene
import javafx.scene.layout.{Priority, VBox}
import javafx.stage.{Stage, StageStyle}

import scala.compiletime.uninitialized
import scala.concurrent.Promise

class window[E](width: Double, height: Double, stage : Stage) extends ElementBuilder[Stage], HasHeaderButtons {

  private val darkMode: DarkModeConf = inject(classOf[DarkModeConf])

  private val promise = Promise[E]()

  private val content = new SimpleObjectProperty[ElementBuilder[?]]()

  private val header = new SimpleObjectProperty[header]()

  private var resizableFlag = true

  lazy val node : Stage = {
    stage.setResizable(resizableFlag)
    stage.initStyle(StageStyle.UNDECORATED)

    val ui = component[VBox] {
      vbox() {
        style = "-fx-border-color: #444; -fx-border-width: 1;"
        titleBar(stage) {
          titleProp(prop => prop <-> titleProperty)

          minimizableProp(prop => prop <-> minimizableProperty)
          maximizableProp(prop => prop <-> maximizableProperty)
          closeableProp(prop => prop <-> closeableProperty)

          reactTo(header) { header => {
            header.children.foreach(child => register(child))
          }}

        }

        reactTo(content) { content => {
          register(content) {
            vgrow = Priority.ALWAYS
          }
        }}

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

    stage.setScene(scene)

    stage
  }

  def add(child: ElementBuilder[?]): Unit = {
    child match
      case h: header =>
        header.set(h)
      case _ =>
        content.set(child)
  }

  def closeWithResult(value: E): Unit =
    node match {
      case w : Window[E] =>
        w.result = Some(value)
        promise.trySuccess(value)
        w.close()
      case _ => node.close()
    }

  override def build(): Stage = node

}

object window {

  def apply[T](width: Double = -1, height: Double = -1, stage : Stage = new Window[T]())(body: (window[T], BuildContext) ?=> Unit)
              (using ctx: BuildContext, parent: ElementBuilder[?]): Stage =
    DSL.create[Stage, window[T]](Ref(), new window[T](width, height, stage))(body)

  def closeWithResult[T](value: T)(using h: window[T], b : ElementBuilder[?], ctx : BuildContext): Unit =
    h.write(() => h.closeWithResult(value))

  def close[T]()(using h: window[T], b : ElementBuilder[?], ctx : BuildContext): Unit =
    h.write(() => h.node.close())

  def resizable[T](using h: window[T], b : ElementBuilder[?], ctx : BuildContext): Boolean =
    h.read(h.resizableFlag)

  def resizable_=[T](value: Boolean)(using h: window[T], b : ElementBuilder[?], ctx : BuildContext): Unit =
    h.write(() => h.resizableFlag = value)

}

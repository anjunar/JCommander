package com.anjunar.jcommander.ui

import com.anjunar.jcommander.components.DarkModeComponent
import com.anjunar.jcommander.configuration.DarkModeConf
import com.anjunar.jcommander.utils.CdiUtils.inject
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ButtonType, Label}
import scalafx.scene.layout.*
import scalafx.stage.{Modality, Stage, StageStyle}

abstract class ThemedDialog[T] extends Stage {

  val darkMode = inject(classOf[DarkModeConf])
  initStyle(StageStyle.Undecorated)
  initModality(Modality.ApplicationModal)

  val titleBar = new MinimalTitleBar(this, "Dialog")

  val headerLabel = new Label("") {
    style = "-fx-font-size: 14px; -fx-font-weight: bold;"
    padding = Insets(10)
  }

  val buttonBox = new HBox(10) {
    padding = Insets(10)
  }

  val contentPane = new VBox(10) {
    padding = Insets(12)
    VBox.setVgrow(this, Priority.Always)
  }

  val root = new VBox {
    style = "-fx-border-color: #444; -fx-border-width: 1;"
    children = Seq(titleBar.node, new BorderPane {
      styleClass = Seq("dialog-pane")
      top = headerLabel
      center = contentPane
      bottom = buttonBox
    })
    VBox.setVgrow(contentPane, Priority.Always)
  }

  scene = new Scene(root) {
    val theme = if (darkMode.value) "dark" else "light"
    stylesheets.add(getClass.getResource(s"/$theme-theme.css").toExternalForm)
  }

  darkMode.valueProperty.addListener { (_, _, isDark) => {
    val theme = if (darkMode.value) "dark" else "light"
    scene.value.getStylesheets.clear()
    scene.value.getStylesheets.add(getClass.getResource(s"/$theme-theme.css").toExternalForm)
  }}

  private var _buttonTypes: Seq[ButtonType] = Seq(ButtonType.OK, ButtonType.Cancel)

  override def title: StringProperty = titleBar.titleLabel.text
  override def title_=(value: String): Unit = titleBar.titleLabel.text.value = value

  def headerText: String = headerLabel.text()
  def headerText_=(value: String): Unit = headerLabel.text = value

  object dialogPane {

    def content: Pane = contentPane

    def content_=(pane: Pane): Unit = {
      contentPane.children.setAll(pane)
    }

    def buttonTypes: Seq[ButtonType] = _buttonTypes

    def buttonTypes_=(types: Seq[ButtonType]): Unit = {
      _buttonTypes = types
      buttonBox.children = types.map { bt =>
        new Button(bt.text) {
          onAction = _ => closeWithResult(bt.asInstanceOf[T])
        }
      }
    }

    def lookupButton(buttonType: ButtonType): javafx.scene.control.Button = buttonBox.children.find(_.asInstanceOf[javafx.scene.control.Button].textProperty().get() == buttonType.text).get.asInstanceOf[javafx.scene.control.Button]
  }

  var resultConverter: T => T = identity.asInstanceOf[T => T]
  var defaultResult: T = null.asInstanceOf[T]

  private val _result = new ObjectProperty[Option[T]](this, "result", None)
  protected def setResult(value: T): Unit = _result.value = Some(value)
  def result: Option[T] = _result()

  def closeWithResult(value: T): Unit = {
    _result.value = Some(resultConverter(value))
    close()
  }

  def showAndWaitDialog(): Option[T] = {
    showAndWait()
    result
  }
}

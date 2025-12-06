package com.anjunar.jcommander.dsl.window

import com.anjunar.javafx.dsl.ChildBuilder.reactTo
import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.traits.HasItems.items
import com.anjunar.javafx.dsl.traits.HasSpacing.alignment
import com.anjunar.javafx.dsl.traits.HasText.text
import com.anjunar.javafx.dsl.traits.HasWidth.prefWidth
import com.anjunar.javafx.dsl.traits.IsNode.{hgrow, position, vgrow, vgrow_=}
import com.anjunar.javafx.dsl.{ElementBuilder, Producer}
import com.anjunar.javafx.scene.control.listView.selectionModel
import com.anjunar.javafx.scene.control.{borderPane, label, listView}
import com.anjunar.javafx.scene.layout.{hbox, vbox}
import com.anjunar.javafx.scene.{header, window}
import com.anjunar.javafx.stage.Window
import com.anjunar.jcommander.dsl.config.ConfigModule
import com.anjunar.jcommander.utils.CdiUtils.*
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.control.MultipleSelectionModel
import javafx.scene.layout.Priority

class ConfigurationWindow extends ElementBuilder[Window[Unit]] {

  private val modules = injectInstance(classOf[ConfigModule])

  private val content = FXCollections.observableArrayList[ElementBuilder[?]](modules.head.getView)

  lazy val node: Window[Unit] = component[Window[Unit]] {
    window[Unit](800, 600) {
      header() {
        label() {
          text = "Configuration"
        }
      }

      hbox() {
        vgrow = Priority.ALWAYS

        listView[String]() {
          prefWidth = 200
          items = FXCollections.observableArrayList(modules.map(_.name) *)
          selectionModel((selectionModel : MultipleSelectionModel[String]) => {
            selectionModel.selectedIndexProperty().addListener((_, _, newValue) => {
              content.clear()
              content.add(modules(newValue.intValue()).getView)
            })
          })
        }

        vbox() {
          hgrow = Priority.ALWAYS
          alignment = Pos.CENTER
          reactTo(content)
        }

      }
    }
  }

  override def build(): Window[Unit] = node
}

object ConfigurationWindow extends Producer[ConfigurationWindow, Window[Unit]] {
  override def createBuilder: ConfigurationWindow = new ConfigurationWindow()
}

package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.*
import com.anjunar.javafx.dsl.traits.HasText.text
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasSpacing.spacing
import com.anjunar.javafx.scene.control.{menu, menuBar, menuItem}
import com.anjunar.javafx.scene.layout.hbox
import com.anjunar.javafx.stage.Window
import javafx.scene.layout.HBox
import com.anjunar.jcommander.utils.AutoBindObservableProperties

class MainMenu extends NodeBuilder[HBox] {

  lazy val node : HBox = {
    val mainMenu = component[HBox] {
      hbox() {
        spacing = 10
        menuBar() {
          menu() {
            text = "File"
            menuItem() {
              text = "New"
            }
            menuItem() {
              text = "Open"
            }
            menuItem() {
              text = "Configuration"
              onAction = event => {
                val dialog = component[Window[Unit]] {
                  ConfigurationWindow() {

                  }
                }

                dialog.show()
              }
            }
          }
        }
      }
    }

    mainMenu
  }

  override def build(): HBox = node
}

object MainMenu extends Producer[MainMenu, HBox] {

  override def createBuilder: MainMenu = new MainMenu()
  
}

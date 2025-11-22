package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.*
import javafx.scene.layout.HBox
import com.anjunar.jcommander.utils.AutoBindObservableProperties
class MainMenu extends NodeBuilder[HBox] {

  lazy val node: HBox = {
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

              }
            }
          }
        }
      }
    }

    AutoBindObservableProperties.bind(this, mainMenu)

    mainMenu
  }

  override def build(): HBox = node
}

object MainMenu extends Producer[MainMenu, HBox] {

  override def createBuilder: MainMenu = new MainMenu()
  
}

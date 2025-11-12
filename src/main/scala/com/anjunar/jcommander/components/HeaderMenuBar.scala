package com.anjunar.jcommander.components

import com.anjunar.jcommander.Component
import jakarta.enterprise.context.ApplicationScoped
import scalafx.beans.property.BooleanProperty
import scalafx.scene.control.*

@ApplicationScoped
class HeaderMenuBar extends Component[MenuBar] {
  
  lazy val node = new MenuBar {
    val fileMenu = new Menu("File") {
      items = List(
        new MenuItem("New") {
          onAction = _ => println("Neu gewählt")
        },
        new MenuItem("Open...") {
          onAction = _ => println("Öffnen gewählt")
        },
        new SeparatorMenuItem,
        new MenuItem("Exit") {
          onAction = _ => System.exit(0)
        }
      )
    }

    val editMenu = new Menu("Edit") {
      items = List(
        new MenuItem("Copy") {
          onAction = _ => println("Kopieren gewählt")
        },
        new MenuItem("Paste") {
          onAction = _ => println("Einfügen gewählt")
        }
      )
    }

    val viewMenu = new Menu("View") {
      items = List(
        new CheckMenuItem("Show Details") {
          selected = true
          onAction = _ => println(s"Details: $selected")
        }
      )
    }

    val configurationMenu = new Menu("Configuration") {
      items = List(
        new MenuItem("Sublime Text") {
          onAction = _ => println("Kopieren gewählt")
        }
      )
    }

    val helpMenu = new Menu("Help") {
      items = List(
        new MenuItem("About...") {
          onAction = _ => new Alert(Alert.AlertType.Information) {
            title = "About JCommander"
            headerText = "JCommander"
            contentText = "A File-Commander with ScalaFX."
          }.showAndWait()
        }
      )
    }

    useSystemMenuBar = false
    menus = List(fileMenu, editMenu, viewMenu, configurationMenu, helpMenu)
  }


}

package com.anjunar.jcommander.components

import com.anjunar.jcommander.commands.QuitCommand
import com.anjunar.jcommander.CdiUtils.*
import com.anjunar.jcommander.ui.ThemedDialog
import jakarta.enterprise.context.ApplicationScoped
import scalafx.scene.control.*
import scalafx.scene.layout.{HBox, VBox}

@ApplicationScoped
class HeaderMenuBarComponent extends Component[HBox] {

  val darkMode = inject(classOf[DarkModeComponent])

  val node = new HBox {
    spacing = 10
    children = Seq(new MenuBar {
      val fileMenu = new Menu("File") {
        items = List(
          new MenuItem("New") {
            onAction = _ => println("Neu gewählt")
          },
          new MenuItem("Open...") {
            onAction = _ => println("Öffnen gewählt")
          },
          new MenuItem("Configuration") {
            onAction = _ => inject(classOf[ConfigurationComponent]).node.showAndWait()
          },
        new SeparatorMenuItem,
          new MenuItem("Exit") {
            onAction = _ => inject(classOf[QuitCommand]).execute()
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

      val helpMenu = new Menu("Help") {
        items = List(
          new MenuItem("About...") {
            onAction = _ => new ThemedDialog[Unit] {
              title = "About JCommander"
              headerText = "JCommander written with Scala FX"
              dialogPane.content = new VBox {
                spacing = 10
                children = Seq(
                  new Label("Version: 1.0.0"),
                  new Label("Author: Patrick Bittner")
                )
              }
            }.showAndWait()
          }
        )
      }

      useSystemMenuBar = false
      menus = List(fileMenu, editMenu, viewMenu, helpMenu)
    })
  }

}

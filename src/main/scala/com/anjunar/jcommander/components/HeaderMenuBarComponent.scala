package com.anjunar.jcommander.components

import com.anjunar.jcommander.commands.QuitCommand
import com.anjunar.jcommander.CdiUtils.*
import jakarta.enterprise.context.ApplicationScoped
import scalafx.scene.control.*
import scalafx.scene.layout.HBox

@ApplicationScoped
class HeaderMenuBarComponent extends Component[HBox] {
  
  val darkMode = inject(classOf[DarkModeComponent])

  lazy val node = new HBox {
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
            onAction = _ => new Alert(Alert.AlertType.Information) {
              title = "About JCommander"
              headerText = "JCommander"
              contentText = "A File-Commander with ScalaFX."
              dialogPane().getStylesheets.add(
                getClass.getResource(s"/${if (darkMode.value) "dark" else "light"}-theme.css").toExternalForm
              )
            }.showAndWait()
          }
        )
      }

      useSystemMenuBar = false
      menus = List(fileMenu, editMenu, viewMenu, helpMenu)
    })
  }

}

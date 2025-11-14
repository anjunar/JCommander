package com.anjunar.jcommander.components

import com.anjunar.jcommander.components.config.ConfigModule
import com.anjunar.jcommander.CdiUtils.*
import com.anjunar.jcommander.ui.MinimalTitleBar
import jakarta.enterprise.context.ApplicationScoped
import scalafx.Includes.jfxMultipleSelectionModel2sfx
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Label, ListView}
import scalafx.scene.layout.{BorderPane, Priority, Region, StackPane, VBox}
import scalafx.stage.{Stage, StageStyle}

@ApplicationScoped
class ConfigurationComponent extends Component[Stage] {

  val modules = injectInstance(classOf[ConfigModule])
  val darkMode = inject(classOf[DarkModeComponent])

  override lazy val node: Stage = new Stage {
    title = "Configuration"
    width = 800
    height = 600
    initStyle(StageStyle.Undecorated)

    val titleBar = new MinimalTitleBar(this, "Configuration")

    val rootContainer: VBox = new VBox {
      style = "-fx-border-color: #444; -fx-border-width: 1;"

      children = Seq(titleBar.node)

      val rootPane: BorderPane = new BorderPane {
        padding = Insets(1, 1, 1, 1)

        val moduleList: ListView[String] = new ListView[String] {
          items.value.addAll(modules.map(_.name) *)
          prefWidth = 200
        }

        val detailContainer = new VBox {
          alignment = Pos.Center
        }

        left = moduleList
        center = detailContainer

        moduleList.selectionModel().selectedIndex.onChange { (_, _, newIndex) =>
          detailContainer.children.clear()
          if (newIndex.intValue() >= 0 && newIndex.intValue() < modules.length) {
            val view = modules(newIndex.intValue()).getView
            view.maxWidth = Region.UsePrefSize
            view.maxHeight = Region.UsePrefSize

            detailContainer.children.add(view)
          }
        }

        if (modules.nonEmpty) moduleList.selectionModel().select(0)
      }

      children.add(rootPane)
      VBox.setVgrow(rootPane, Priority.Always)
    }

    scene = new Scene(rootContainer) {
      stylesheets.add(
        getClass.getResource(s"/${if (darkMode.value) "dark" else "light"}-theme.css").toExternalForm
      )
    }
  }
}

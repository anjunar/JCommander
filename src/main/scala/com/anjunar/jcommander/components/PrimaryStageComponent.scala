package com.anjunar.jcommander.components

import com.anjunar.jcommander.commands.QuitCommand
import com.anjunar.jcommander.configuration.PrimaryStageConf
import com.anjunar.jcommander.utils.CdiUtils.*
import com.anjunar.jcommander.ui.{Resizable, TitleBar}
import jakarta.enterprise.context.ApplicationScoped
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.layout.{Priority, VBox}
import scalafx.stage.StageStyle
import scalafx.Includes.*
import scalafx.scene.image.Image

class PrimaryStageComponent extends Component[JFXApp3.PrimaryStage] {

  val primaryStageConf: PrimaryStageConf = inject(classOf[PrimaryStageConf])
  val rootPane: RootPaneComponent = new RootPaneComponent()

  override val node: JFXApp3.PrimaryStage = new JFXApp3.PrimaryStage {
    title = "JCommander File Manager"
    width = primaryStageConf.width
    height = primaryStageConf.height
    icons += new Image(getClass.getResourceAsStream("/icon.ico"))

    width.onChange { (_, _, newValue) => primaryStageConf.width = newValue.doubleValue }
    height.onChange { (_, _, newValue) => primaryStageConf.height = newValue.doubleValue }

    val titleBar = new TitleBar(this)

    val container: VBox = new VBox {
      style = "-fx-border-color: #444; -fx-border-width: 1;"
      children = Seq(titleBar.node, rootPane.node)
    }

    VBox.setVgrow(rootPane.node, Priority.Always)

    scene = new Scene(container)

    new Resizable(this, container)

    onCloseRequest = _ => inject(classOf[QuitCommand]).execute()
    initStyle(StageStyle.Undecorated)
  }
}

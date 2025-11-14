package com.anjunar.jcommander.components

import com.anjunar.jcommander.commands.QuitCommand
import com.anjunar.jcommander.configuration.PrimaryStageConf
import com.anjunar.jcommander.CdiUtils.*
import jakarta.enterprise.context.ApplicationScoped
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.layout.{Priority, VBox}
import scalafx.stage.StageStyle
import scalafx.Includes.*
import scalafx.scene.image.Image

@ApplicationScoped
class PrimaryStageComponent extends Component[JFXApp3.PrimaryStage] {

  val primaryStageConf: PrimaryStageConf = inject(classOf[PrimaryStageConf])
  val rootPane: RootPaneComponent = inject(classOf[RootPaneComponent])

  override lazy val node: JFXApp3.PrimaryStage = new JFXApp3.PrimaryStage {
    title = "JCommander File Manager"
    width = primaryStageConf.width
    height = primaryStageConf.height
    icons += new Image(getClass.getResourceAsStream("/icon.ico"))

    val titleBar = new TitleBarComponent(this)

    val container: VBox = new VBox {
      style = "-fx-border-color: #444; -fx-border-width: 1;"
      children = Seq(titleBar.node, rootPane.node)
    }

    VBox.setVgrow(rootPane.node, Priority.Always)

    scene = new Scene(container)

    new ResizableComponent(this, container)

    onCloseRequest = _ => inject(classOf[QuitCommand]).execute()
    initStyle(StageStyle.Undecorated)
  }
}

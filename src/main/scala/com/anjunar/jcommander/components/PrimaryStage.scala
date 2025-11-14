package com.anjunar.jcommander.components

import com.anjunar.jcommander.commands.QuitCommand
import com.anjunar.jcommander.configuration.PrimaryStageConf
import com.anjunar.jcommander.inject
import jakarta.enterprise.context.ApplicationScoped
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.layout.{Priority, VBox}
import scalafx.stage.StageStyle
import scalafx.Includes.*
import scalafx.scene.image.Image

@ApplicationScoped
class PrimaryStage extends Component[JFXApp3.PrimaryStage] {

  val primaryStageConf: PrimaryStageConf = inject(classOf[PrimaryStageConf])
  val rootPane: RootPane = inject(classOf[RootPane])

  override lazy val node: JFXApp3.PrimaryStage = new JFXApp3.PrimaryStage {
    val titleBar = new TitleBar(this)

    title = "JCommander File Manager"
    width = primaryStageConf.width
    height = primaryStageConf.height
    icons += new Image(getClass.getResourceAsStream("/icon.ico"))

    val container: VBox = new VBox {
      children = Seq(titleBar.node, rootPane.node)
    }

    VBox.setVgrow(rootPane.node, Priority.Always)

    scene = new Scene(container)

    new Resizable(this, container)

    onCloseRequest = _ => inject(classOf[QuitCommand]).execute()
    initStyle(StageStyle.Undecorated)
  }
}

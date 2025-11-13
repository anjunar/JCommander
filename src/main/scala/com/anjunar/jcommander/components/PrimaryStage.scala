package com.anjunar.jcommander.components

import com.anjunar.jcommander.configuration.PrimaryStageConf
import com.anjunar.jcommander.inject
import jakarta.enterprise.context.ApplicationScoped
import scalafx.application.JFXApp3
import scalafx.scene.Scene

@ApplicationScoped
class PrimaryStage extends Component[JFXApp3.PrimaryStage] {

  val primaryStageConf = inject(classOf[PrimaryStageConf])
  
  val rootPane = inject(classOf[RootPane])

  override lazy val node: JFXApp3.PrimaryStage = new JFXApp3.PrimaryStage {
    title = "JCommander File Manager"
    width = primaryStageConf.width
    height = primaryStageConf.height
    scene = new Scene(rootPane.node)
  }
  
}

package com.anjunar.javafx.scene

import javafx.scene.SceneAntialiasing
import scalafx.stage.Stage

class Window[E] extends Stage {
  
  var result : Option[E] = None

  def showAndWaitResult(): Option[E] = {
    showAndWait()
    result
  }
}

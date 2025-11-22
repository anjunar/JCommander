package com.anjunar.javafx.stage

import scalafx.stage.Stage

class Window[E] extends Stage {
  
  var result : Option[E] = None

  def showAndWaitResult(): Option[E] = {
    showAndWait()
    result
  }
}

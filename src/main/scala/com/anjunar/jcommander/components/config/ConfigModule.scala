package com.anjunar.jcommander.components.config

import scalafx.scene.layout.VBox

trait ConfigModule {
  def name: String
  def getView: VBox
}

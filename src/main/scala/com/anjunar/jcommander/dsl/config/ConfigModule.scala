package com.anjunar.jcommander.dsl.config

import com.anjunar.javafx.scene.layout.vbox

trait ConfigModule {

  def name: String

  def getView: vbox


}

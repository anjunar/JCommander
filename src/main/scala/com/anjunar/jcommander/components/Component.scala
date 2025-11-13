package com.anjunar.jcommander.components

import scalafx.scene.Node

trait Component[N] {
  
  lazy val node: N

}

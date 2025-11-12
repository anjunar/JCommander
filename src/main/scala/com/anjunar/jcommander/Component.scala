package com.anjunar.jcommander

import scalafx.scene.Node

trait Component[N <: Node] {
  
  val node: N

}

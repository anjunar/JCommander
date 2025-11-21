package com.anjunar.javafx.dsl

import javafx.scene.Node

trait ElementBuilder[E] {
  def build(): E
}
package com.anjunar.javafx.dsl

import com.anjunar.jcommander.utils.AutoBindObservableProperties

trait ElementBuilder[E] {

  lazy val node: E = AutoBindObservableProperties.bind(this, create())

  def create(): E
  
  def build(): E
}
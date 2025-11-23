package com.anjunar.javafx.dsl

import com.anjunar.jcommander.utils.AutoBindObservableProperties

trait ElementBuilder[E] {
  def build(): E
}
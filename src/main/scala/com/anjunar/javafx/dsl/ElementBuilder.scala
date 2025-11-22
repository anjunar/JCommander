package com.anjunar.javafx.dsl

trait ElementBuilder[E] {
  def build(): E
}
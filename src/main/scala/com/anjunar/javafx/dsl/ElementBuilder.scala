package com.anjunar.javafx.dsl

trait ElementBuilder[E] {
  
  lazy val node : AnyRef
  
  def build(): E
}
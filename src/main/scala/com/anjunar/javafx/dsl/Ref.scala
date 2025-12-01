package com.anjunar.javafx.dsl

import scala.compiletime.uninitialized

class Ref[R] {
  
  var value : R = uninitialized
  
  def get: R = value
  
  def set(v: R): Unit = value = v

  inline def apply(body: R ?=> Unit): Unit =
    body(using value)

}

object Ref {
  
  def apply[R](): Ref[R] = {
    new Ref[R]()
  }
  
}

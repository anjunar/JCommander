package com.anjunar.javafx.dsl

import scala.compiletime.uninitialized

class Ref[R] {
  
  var value : R = uninitialized

  inline def apply(body: R ?=> Unit): Unit =
    body(using value)

}

object Ref {
  
  def apply[R](): Ref[R] = {
    new Ref[R]()
  }
  
}

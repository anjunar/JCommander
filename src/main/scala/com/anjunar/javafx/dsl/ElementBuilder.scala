package com.anjunar.javafx.dsl

import scala.collection.mutable

trait ElementBuilder[E] {
  def build(): E

  def afterBuild() : Unit = {}

  val applyValues = mutable.ListBuffer[() => Unit]()

  var lifeCycle: LifeCycle = LifeCycle.Build

  def write(f: () => Unit): Unit = {
    if (lifeCycle == LifeCycle.Finished) {
      f()
    } else {
      applyValues.append(f)
    }
  }

  def read[E](value: E): E = {
    if (lifeCycle == LifeCycle.Finished) {
      value
    } else {
      throw new IllegalStateException(
        s"Cannot read UI properties during the $lifeCycle phase (only allowed in Finished)."
      )
    }
  }


}
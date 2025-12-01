package com.anjunar.javafx.dsl

trait Producer[B <: ElementBuilder[? <: R], R] {
  
  def createBuilder : B

  def apply(ref: Ref[B] = Ref())(body: (B, BuildContext) ?=> Unit)
                 (using ctx: BuildContext, parent: ElementBuilder[?]): R =
    DSL.create[R, B](ref, createBuilder)(body)

  def build(ref: Ref[B] = Ref())(body: (B, BuildContext) ?=> Unit) : B = {
    DSL.createBuilder[R, B](ref, createBuilder)(body)(using ctx = new BuildContext)
  }



}

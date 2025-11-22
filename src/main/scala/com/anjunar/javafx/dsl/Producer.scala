package com.anjunar.javafx.dsl

trait Producer[B <: ElementBuilder[? <: R], R] {
  
  def createBuilder : B

  def apply(ref: Ref[B] = Ref())(body: (B, BuildContext) ?=> Unit)
                 (using ctx: BuildContext, parent: ElementBuilder[?]): R =
    DSL.create[R, B](ref, createBuilder)(body)

  def build(ref: Ref[B] = Ref())(body: (B, BuildContext) ?=> Unit) : B = {
    val builder = createBuilder
    body(using builder, new BuildContext)
    builder
  }
    


}

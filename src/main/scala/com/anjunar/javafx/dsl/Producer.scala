package com.anjunar.javafx.dsl

trait Producer[B <: ElementBuilder[? <: R], R] {
  
  def createBuilder : B

  def apply(ref: Ref[B] = Ref())(body: (B, BuildContext) ?=> Unit)
                 (using ctx: BuildContext, parent: ElementBuilder[?]): R =
    DSL.create[R, B](ref, createBuilder)(body)

  def unwrap(ref : String)(body: (B, BuildContext) ?=> Unit)
            (using ctx: BuildContext, parent: ElementBuilder[?]): Unit = {
    val resolvedClass = parent.getClass
    val field = resolvedClass.getDeclaredField(ref + "Ref")
    field.setAccessible(true)
    val refB = field.get(parent).asInstanceOf[Ref[B]]
    if (refB.get != null)
      body(using refB.get, ctx)
  }


  def build(ref: Ref[B] = Ref())(body: (B, BuildContext) ?=> Unit) : B = {
    DSL.createBuilder[R, B](ref, createBuilder)(body)(using ctx = new BuildContext)
  }



}

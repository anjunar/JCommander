package com.anjunar.javafx.dsl

import com.anjunar.scala.universe.TypeResolver

trait Producer[B <: ElementBuilder[? <: R], R] {
  
  def createBuilder : B

  def apply(ref: Ref[B] = Ref())(body: (B, BuildContext) ?=> Unit)
                 (using ctx: BuildContext, parent: ElementBuilder[?]): R =
    DSL.create[R, B](ref, createBuilder)(body)
    
  def unwrap(ref : String)(body: (B, BuildContext) ?=> Unit)
            (using ctx: BuildContext, parent: ElementBuilder[?]): Unit = {
    val resolvedClass = TypeResolver.resolve(parent.getClass)
    val resolvedMethod = resolvedClass.findMethod(ref + "Ref")
    val value = resolvedMethod.invoke(parent).asInstanceOf[Ref[B]].get
    body(using value, ctx)
  }
      

  def build(ref: Ref[B] = Ref())(body: (B, BuildContext) ?=> Unit) : B = {
    DSL.createBuilder[R, B](ref, createBuilder)(body)(using ctx = new BuildContext)
  }



}

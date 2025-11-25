package com.anjunar.javafx.dsl

import com.anjunar.javafx.scene.{header, window}
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.scene.control.Button

object DSL {

  def component[C](body: (ElementBuilder[C], BuildContext) ?=> Unit): C =
    given ctx: BuildContext = BuildContext()

    val root = ComponentBuilder[C]()

    body(using root, ctx)
    root.build()

  def createBuilder[T, B <: ElementBuilder[? <: T]](ref : Ref[B], construct: B)(body: (B, BuildContext) ?=> Unit)
                                            (using ctx: BuildContext): B =

    val builder = construct

    ctx.stack.push(builder)

    ref.value = builder
    builder.lifeCycle = LifeCycle.Build

    val node = builder.build()

    body(using builder, ctx)

    builder.lifeCycle = LifeCycle.Apply
    builder.applyValues.foreach(fn => fn())
    builder.applyValues.clear()

    builder.lifeCycle = LifeCycle.Bind

    builder match {
      case builder : ChildNodeBuilder[?,?] =>
        AutoBindObservableProperties.observeList(builder.children, builder.fxObservableList)
      case builder : ChildElementBuilder[?, ?] =>
        AutoBindObservableProperties.observeList(builder.children, builder.fxObservableList)
      case _ => ()
    }

    builder

  def create[T, B <: ElementBuilder[? <: T]](ref : Ref[B], construct: B)(body: (B, BuildContext) ?=> Unit)
                                               (using ctx: BuildContext, parent: ElementBuilder[?]): T =

    val builder = construct
    ref.value = builder

    builder.lifeCycle = LifeCycle.Build

    val node = builder.build()

    body(using builder, ctx)

    builder.lifeCycle = LifeCycle.Hook

    parent match {
      case children: ChildNodeBuilder[?, ?] => children.add(builder)
      case children: ChildElementBuilder[?, ?] => children.add(builder)
      case component: ComponentBuilder[?] => component.add(builder)
      case stage: window[?] => stage.add(builder)
      case header: header => header.add(builder)
      case _ => ()
    }

    builder.lifeCycle = LifeCycle.Apply
    builder.applyValues.foreach(fn => fn())
    builder.applyValues.clear()


    builder.lifeCycle = LifeCycle.Bind

    builder match {
      case builder : ChildNodeBuilder[?,?] =>
        AutoBindObservableProperties.observeList(builder.children, builder.fxObservableList)
      case builder : ChildElementBuilder[?, ?] =>
        AutoBindObservableProperties.observeList(builder.children, builder.fxObservableList)
      case _ => ()
    }

    builder.lifeCycle = LifeCycle.Finished
    builder.afterBuild()
    
    builder match {
      case builder : NodeBuilder[?] => builder.registerLayoutListener()
      case _ => ()
    }

    node

  extension [A](src: javafx.beans.property.Property[A])
    infix def <->(target: javafx.beans.property.Property[A]): Unit =
      src.bindBidirectional(target)
  
}

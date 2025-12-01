package com.anjunar.javafx.dsl

import com.anjunar.javafx.dsl.traits.IsNode
import com.anjunar.javafx.scene.layout.gridPane
import com.anjunar.javafx.scene.{header, window}
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.GridPane

object DSL {

  def component[C](body: (ElementBuilder[C], BuildContext) ?=> Unit): C =
    given ctx: BuildContext = BuildContext()

    val root = ComponentBuilder[C]()

    body(using root, ctx)

    val node = root.build()

    ctx.stack.foreach(elem => {
      elem.afterBuild()
    })

    node

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
      case gridPane : gridPane =>
        AutoBindObservableProperties.observeList(gridPane.children, gridPane.fxObservableList, elem => {
          val node = elem.build().asInstanceOf[Node]
          GridPane.setConstraints(node, elem.asInstanceOf[IsNode].gridPaneX, elem.asInstanceOf[IsNode].gridPaneY)
          node
        })
      case builder : ChildNodeBuilder[?,Any] =>
        AutoBindObservableProperties.observeList(builder.children, builder.fxObservableList, elem => elem.build())
      case builder : ChildElementBuilder[?, Any] =>
        AutoBindObservableProperties.observeList(builder.children, builder.fxObservableList, elem => elem.build())
      case _ => ()
    }

    builder

  def create[T, B <: ElementBuilder[? <: T]](ref : Ref[B], construct: B)(body: (B, BuildContext) ?=> Unit)
                                               (using ctx: BuildContext, parent: ElementBuilder[?]): T =

    val builder = construct
    ref.value = builder

    ctx.stack.push(builder)

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
      case gridPane : gridPane =>
        AutoBindObservableProperties.observeList(gridPane.children, gridPane.fxObservableList, elem => {
          val node = elem.build().asInstanceOf[Node]
          GridPane.setConstraints(node, elem.asInstanceOf[IsNode].gridPaneX, elem.asInstanceOf[IsNode].gridPaneY)
          node
        })
      case builder : ChildNodeBuilder[?,Any] =>
        AutoBindObservableProperties.observeList(builder.children, builder.fxObservableList, elem => elem.build())
      case builder : ChildElementBuilder[?, Any] =>
        AutoBindObservableProperties.observeList(builder.children, builder.fxObservableList, elem => elem.build())
      case _ => ()
    }

    builder.lifeCycle = LifeCycle.Finished

    builder match {
      case builder : NodeBuilder[?] => builder.registerLayoutListener()
      case _ => ()
    }

    node

  extension [A](src: javafx.beans.property.Property[A])
    infix def <->(target: javafx.beans.property.Property[A]): Unit =
      src.bindBidirectional(target)
  
}

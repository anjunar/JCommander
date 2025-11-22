package com.anjunar.javafx.dsl

object DSL {

  def component[C](body: (ElementBuilder[C], BuildContext) ?=> Unit): C =
    given ctx: BuildContext = BuildContext()

    val root = ComponentBuilder[C]()

    body(using root, ctx)
    root.build()

  def create[T, B <: ElementBuilder[? <: T]](ref : Ref[B], construct: B)(body: (B, BuildContext) ?=> Unit)
                                               (using ctx: BuildContext, parent: ElementBuilder[?]): T =

    val builder = construct
    ref.value = builder

    parent match {
      case children : ChildBuilder[?] => children.add(builder)
      case component : ComponentBuilder[?] => component.add(builder)
      case stage : window[?] => stage.add(builder)
      case header : header => header.add(builder)
      case _ => ()
    }

    body(using builder, ctx)
    builder.build()

  export com.anjunar.javafx.dsl.traits.HasSpacing.*
  export com.anjunar.javafx.dsl.traits.HasOnAction.*
  export com.anjunar.javafx.dsl.traits.HasLabeled.*
  export com.anjunar.javafx.dsl.traits.HasText.*
  export com.anjunar.javafx.dsl.traits.HasNode.*
  export com.anjunar.javafx.dsl.traits.HasWidth.*
  export com.anjunar.javafx.scene.titleBar
  export com.anjunar.javafx.scene.header
  export com.anjunar.javafx.scene.window
  export com.anjunar.javafx.scene.window.HasWindow.*
  export com.anjunar.javafx.scene.control.button
  export com.anjunar.javafx.scene.control.progressBar
  export com.anjunar.javafx.scene.control.progressBar.HasProgressBar.*
  export com.anjunar.javafx.scene.control.checkbox
  export com.anjunar.javafx.scene.control.checkbox.HasCheckBox.*
  export com.anjunar.javafx.scene.control.label
  export com.anjunar.javafx.scene.control.menu
  export com.anjunar.javafx.scene.control.menuBar
  export com.anjunar.javafx.scene.control.menuItem
  export com.anjunar.javafx.scene.control.splitPane
  export com.anjunar.javafx.scene.control.splitPane.HasDividerPosition.*
  export com.anjunar.javafx.scene.control.tableView
  export com.anjunar.javafx.scene.control.tableView.HasTableView.*
  export com.anjunar.javafx.scene.control.tableColumn
  export com.anjunar.javafx.scene.control.tableColumn.HasTableColumn.*
  export com.anjunar.javafx.scene.layout.vbox
  export com.anjunar.javafx.scene.layout.hbox
  export com.anjunar.javafx.scene.layout.region
  export com.anjunar.javafx.scene.image.ImageView
  export com.anjunar.javafx.scene.image.ImageView.HasImageView.*
  export com.anjunar.javafx.dsl.ChildBuilder.*

}

package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.*
import com.anjunar.javafx.dsl.traits.HasNode
import javafx.scene.control

class tableView[E] extends ChildBuilder[control.TableView[E]] {

  override val node: control.TableView[E] = new control.TableView[E]()

  override def add(child: ElementBuilder[?]): Unit = {
    node.getColumns.add(child.build().asInstanceOf[control.TableColumn[E, ?]])
  }

  override def build(): control.TableView[E] = node

}

object tableView {

  def apply[E](ref: Ref[tableView[E]] = Ref[tableView[E]]())(body: (tableView[E], BuildContext) ?=> Unit)
              (using ctx: BuildContext, parent: ElementBuilder[?]): control.TableView[E] =
    DSL.create[control.TableView[E], tableView[E]](ref, new tableView[E]())(body)


}

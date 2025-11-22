package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.*
import com.anjunar.javafx.dsl.traits.{HasHeight, HasNode, HasWidth}
import javafx.scene.control
import javafx.scene.control.TableView
import javafx.util.Callback

import scala.compiletime.uninitialized

class tableView[E] extends ChildBuilder[control.TableView[E]], HasWidth, HasHeight {

  override val node: control.TableView[E] = new control.TableView[E]()

  var sortPolicy : TableView[E] => Boolean = uninitialized
  
  override def add(child: ElementBuilder[?]): Unit = {
    node.getColumns.add(child.build().asInstanceOf[control.TableColumn[E, ?]])
  }

  override def build(): control.TableView[E] = node

}

object tableView {

  def apply[E](ref: Ref[tableView[E]] = Ref[tableView[E]]())(body: (tableView[E], BuildContext) ?=> Unit)
              (using ctx: BuildContext, parent: ElementBuilder[?]): control.TableView[E] =
    DSL.create[control.TableView[E], tableView[E]](ref, new tableView[E]())(body)
  
  object HasTableView {
    
    def sortPolicy[E]()(using h: tableView[E]) : TableView[E] => Boolean = h.sortPolicy
    def sortPolicy_=[E](v: TableView[E] => Boolean)(using h: tableView[E]) : Unit = {
      h.sortPolicy = v
      h.node.setSortPolicy((param: TableView[E]) => v(param))
    }

    def selectionModel[E]()(using h: tableView[E]) : control.TableView.TableViewSelectionModel[E] = h.node.getSelectionModel
    
  }

}

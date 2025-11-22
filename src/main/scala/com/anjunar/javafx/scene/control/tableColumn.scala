package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.{HasWidth, HasText}
import com.anjunar.javafx.dsl.{BuildContext, DSL, ElementBuilder, Producer, Ref}
import com.anjunar.jcommander.files.FileItem
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.scene.control
import javafx.scene.control.{TableCell, TableColumn}
import javafx.util.Callback

import scala.compiletime.uninitialized

class tableColumn[E, T] extends ElementBuilder[control.TableColumn[E, T]], HasText, HasWidth {

  lazy val node: control.TableColumn[E, T] = new control.TableColumn[E, T]()
  
  var cellFactory : (T, Boolean, TableCell[E,T]) => Unit = uninitialized
  var cellValueFactory : E => T = uninitialized

  override def build(): control.TableColumn[E, T] = node

}

object tableColumn {

  def apply[E, T](ref: Ref[tableColumn[E,T]] = Ref[tableColumn[E,T]]())(body: (tableColumn[E,T], BuildContext) ?=> Unit)
                 (using ctx: BuildContext, parent: ElementBuilder[?]): control.TableColumn[E,T] =
    DSL.create[control.TableColumn[E,T], tableColumn[E,T]](ref, new tableColumn[E,T]())(body)


  object HasTableColumn {
    
    def reorderable[E, T]()(using h: tableColumn[E,T]) : Boolean = h.node.isReorderable
    def reorderable_=[E, T](v: Boolean)(using h: tableColumn[E,T]) : Unit = h.node.setReorderable(v)

    def cellFactory[E, T]()(using h: tableColumn[E, T]): (T, Boolean, TableCell[E,T]) => Unit =
      h.cellFactory

    def cellFactory_=[E, T](v: (T, Boolean, TableCell[E,T]) => Unit)(using h: tableColumn[E, T]): Unit = { 
      h.cellFactory = v
      h.node.setCellFactory((p: control.TableColumn[E, T]) => new TableCell[E, T]() {
        override def updateItem(item: T, empty: Boolean): Unit = { 
          super.updateItem(item, empty)
          v(item, empty, this)
        }
      })
    }
    
    def cellValueFactory[E,T]()(using h: tableColumn[E,T]) : E => T = h.cellValueFactory
    def cellValueFactory_=[E,T](converter : E => T)(using h: tableColumn[E,T]) : Unit = {
      h.cellValueFactory = converter
      h.node.setCellValueFactory((p: control.TableColumn.CellDataFeatures[E, T]) => new SimpleObjectProperty[T](converter(p.getValue)))
    }

  }

}

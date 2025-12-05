package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasPadding.padding
import com.anjunar.javafx.dsl.traits.HasSpacing.{alignment, spacing}
import com.anjunar.javafx.dsl.traits.HasText.text
import com.anjunar.javafx.dsl.traits.IsNode.vgrow
import com.anjunar.javafx.dsl.{BuildContext, DSL, ElementBuilder, Producer, Ref}
import com.anjunar.javafx.scene.control.checkbox.selectedProperty
import com.anjunar.javafx.scene.control.{button, checkbox, label}
import com.anjunar.javafx.scene.{header, window}
import com.anjunar.javafx.scene.layout.{hbox, region, vbox}
import com.anjunar.javafx.scene.window.closeWithResult
import com.anjunar.javafx.stage.Window
import javafx.beans.property.BooleanProperty
import javafx.geometry.{Insets, Pos}
import javafx.scene.layout.Priority

class ConfirmDialog(isDelete : Boolean) extends ElementBuilder[Window[String]] {
  
  private val headerRef = Ref[label]()
  private val confirmRef = Ref[label]()
  
  private val moveToRecycleRef = Ref[checkbox]()
  private val replaceExistingRef = Ref[checkbox]()

  lazy val node : Window[String] = component[Window[String]] {
    window() {
      header() {
        label(headerRef) { }
      }
      vbox() {
        spacing = 12
        padding = new Insets(10)

        label(confirmRef) {}

        if (isDelete) {
          checkbox(moveToRecycleRef) {
            text = "Move to Recycle Bin"
          }
        } else {
          checkbox(replaceExistingRef) {
            text = "Replace existing files"
          }
        }

        region() {
          vgrow = Priority.ALWAYS
        }

        hbox() {
          spacing = 10
          alignment = Pos.CENTER_RIGHT

          button() {
            text = "Cancel"
            onAction = _ => closeWithResult("Cancel")
          }
          button() {
            text = "OK"
            onAction = _ => closeWithResult("Ok")
          }
        }
      }
    }
  }

  override def build(): Window[String] = node
}

object ConfirmDialog {
  
  def apply(isDelete : Boolean, ref: Ref[ConfirmDialog] = Ref())(body: (ConfirmDialog, BuildContext) ?=> Unit)
           (using ctx: BuildContext, parent: ElementBuilder[?]): Window[String] =
    DSL.create[Window[String], ConfirmDialog](ref, new ConfirmDialog(isDelete))(body)
    
  def confirmHeader(using h: ConfirmDialog) : String = 
    h.read(h.headerRef.get.node.getText)
  def confirmHeader_=(value : String)(using h: ConfirmDialog) : Unit = 
    h.write(() => h.headerRef.get.node.setText(value))

  def confirmText(using h: ConfirmDialog) : String =
    h.read(h.confirmRef.get.node.getText)
  def confirmText_=(value : String)(using h: ConfirmDialog) : Unit =
    h.write(() => h.confirmRef.get.node.setText(value))
    
    
  def moveToRecycle(using h: ConfirmDialog) : (BooleanProperty => Unit) => Unit =
    (f : BooleanProperty => Unit) => h.write(() => f(h.moveToRecycleRef.get.node.selectedProperty()))
  def replaceExisting(using h: ConfirmDialog) : (BooleanProperty => Unit) => Unit =
    (f : BooleanProperty => Unit) => h.write(() => f(h.replaceExistingRef.get.node.selectedProperty()))

}

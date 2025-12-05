package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.{BuildContext, DSL, ElementBuilder, Ref}
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasPadding.padding
import com.anjunar.javafx.dsl.traits.HasSpacing.{alignment, spacing}
import com.anjunar.javafx.dsl.traits.HasText.{text, textProperty, text_=}
import com.anjunar.javafx.dsl.traits.HasWidth.prefWidth
import com.anjunar.javafx.dsl.traits.IsNode.vgrow
import com.anjunar.javafx.scene.control.progressBar.progressProperty
import com.anjunar.javafx.scene.control.{button, label, progressBar}
import com.anjunar.javafx.scene.{header, window}
import com.anjunar.javafx.scene.layout.{hbox, region, vbox}
import com.anjunar.javafx.scene.window.close
import com.anjunar.javafx.stage.Window
import com.typesafe.scalalogging.Logger
import javafx.beans.property.{DoubleProperty, StringProperty}
import javafx.concurrent.Task
import javafx.geometry.{Insets, Pos}
import javafx.scene.layout.Priority

import java.util.concurrent.atomic.AtomicBoolean

class ProgressDialog(canceledFlag: AtomicBoolean, task : Task[Unit]) extends ElementBuilder[Window[Unit]] {

  private val log = Logger[ProgressDialog]

  private val progressTextRef = Ref[label]()
  private val progressBarRef = Ref[progressBar]()
  private val progressRef = Ref[label]()
  private val fileRef = Ref[label]()

  lazy val node : Window[Unit] = component[Window[Unit]] {
    window() {
      header() {
        label() {
          text = "Progress"
        }
      }

      vbox() {
        spacing = 14
        padding = new Insets(20)

        label(progressTextRef) {
          text = "On Progress"
        }

        progressBar(progressBarRef) {
          prefWidth = 380
          progressProperty(prop => prop.bind(task.progressProperty()))
        }

        label(progressRef) {
          text = "Progress: 0%"
        }

        label(fileRef) {
          text = "File: "
        }

        region() {
          vgrow = Priority.ALWAYS
        }

        hbox() {
          alignment = Pos.CENTER_RIGHT
          button() {
            text = "Cancel"
            onAction = _ => {
              canceledFlag.set(true)
              task.cancel()
              close()
              log.info("Operation cancelledFlag by user.")
            }
          }
        }
      }
    }
  }

  override def build(): Window[Unit] = node
}

object ProgressDialog {

  def apply(canceledFlag: AtomicBoolean, task : Task[Unit], ref: Ref[ProgressDialog] = Ref())(body: (ProgressDialog, BuildContext) ?=> Unit)
           (using ctx: BuildContext, parent: ElementBuilder[?]): Window[Unit] =
    DSL.create[Window[Unit], ProgressDialog](ref, new ProgressDialog(canceledFlag, task))(body)

}
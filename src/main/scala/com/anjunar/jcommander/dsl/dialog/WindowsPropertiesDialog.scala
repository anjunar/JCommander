package com.anjunar.jcommander.dsl.dialog

import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasPadding.padding
import com.anjunar.javafx.dsl.traits.HasSpacing.{alignment, spacing}
import com.anjunar.javafx.dsl.traits.HasStyle.style
import com.anjunar.javafx.dsl.traits.HasText.{text, textProperty}
import com.anjunar.javafx.dsl.traits.HasWidth.prefWidth
import com.anjunar.javafx.dsl.traits.IsNode.{gridX, gridY}
import com.anjunar.javafx.dsl.traits.IstTextInput.promptText
import com.anjunar.javafx.dsl.*
import com.anjunar.javafx.scene.control.checkbox.{allowIndeterminate, indeterminate, selected}
import com.anjunar.javafx.scene.control.{button, checkbox, label, textField}
import com.anjunar.javafx.scene.layout.gridPane.{hgap, vgap}
import com.anjunar.javafx.scene.layout.{gridPane, hbox, vbox}
import com.anjunar.javafx.scene.window.close
import com.anjunar.javafx.scene.{header, window}
import com.anjunar.javafx.stage.Window
import javafx.geometry.{Insets, Pos}
import javafx.scene.Node

import java.io.File
import java.nio.file.attribute.DosFileAttributes
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters.*

class WindowsPropertiesDialog(files: Seq[String]) extends ElementBuilder[Window[Unit]] {

  lazy val node: Window[Unit] = {
    val readOnly = Ref[checkbox]()
    val hidden = Ref[checkbox]()
    val recursive = Ref[checkbox]()
    val applyButton = Ref[button]()

    def setTriState(cb: Ref[checkbox], values: Seq[Boolean])(using BuildContext): Unit =
      cb {
        allowIndeterminate = true
        val allTrue = values.forall(_ == true)
        val allFalse = values.forall(_ == false)
        if allTrue then
          indeterminate = false
          selected = true
        else if allFalse then
          indeterminate = false
          selected = false
        else
          indeterminate = true
      }

    def installMixedFix(cb: Ref[checkbox])(using BuildContext): Unit =
      cb {
        onAction = _ => {
          if indeterminate then
            indeterminate = false
            selected = true
        }
      }

    def readDosAttributes(path: String): Option[DosFileAttributes] =
      try
        Some(Files.readAttributes(Paths.get(path), classOf[DosFileAttributes]))
      catch
        case _: Exception => None

    def initSingle()(using BuildContext): Unit =
      val f = files.head
      readDosAttributes(f).foreach { attrs =>
        readOnly {
          allowIndeterminate = false
          selected = attrs.isReadOnly
        }
        hidden {
          allowIndeterminate = false
          selected = attrs.isHidden
        }
      }

    def initMultiple()(using BuildContext): Unit =
      val attrs = files.map(readDosAttributes)
      val roValues = attrs.map(_.exists(_.isReadOnly))
      val hValues = attrs.map(_.exists(_.isHidden))
      setTriState(readOnly, roValues)
      setTriState(hidden, hValues)

    def applyAttributes(): Unit =
      val recursiveFlag = recursive.get.node.isSelected
      val roInd = readOnly.get.node.isIndeterminate
      val hidInd = hidden.get.node.isIndeterminate
      val roVal = readOnly.get.node.isSelected
      val hidVal = hidden.get.node.isSelected

      def applyToPath(p: java.nio.file.Path): Unit =
        if !roInd then
          try Files.setAttribute(p, "dos:readonly", java.lang.Boolean.valueOf(roVal))
          catch case _: Exception => ()
        if !hidInd then
          try Files.setAttribute(p, "dos:hidden", java.lang.Boolean.valueOf(hidVal))
          catch case _: Exception => ()

      files.foreach { f =>
        val p = Paths.get(f)
        if recursiveFlag && Files.isDirectory(p) then
          try
            val stream = Files.walk(p)
            try
              stream.iterator().asScala.foreach(applyToPath)
            finally
              stream.close()
          catch
            case _: Exception => ()
        else
          applyToPath(p)
      }

    val labelText =
      if files.size == 1 then new File(files.head).getName
      else s"${files.size} files"

    component[Window[Unit]] {
      window(380) {

        header() {
          label() {
            text = "Properties"
            style = "-fx-font-size: 18px; -fx-font-weight: bold;"
          }
        }

        vbox() {
          spacing = 12
          padding = Insets(14, 18, 14, 18)

          label() {
            text = labelText
            style = "-fx-font-size: 14px; -fx-font-weight: bold;"
            padding = Insets(0, 0, 4, 0)
          }

          label() {
            text = "Attributes"
            style = "-fx-font-size: 13.5px; -fx-font-weight: bold;"
            padding = Insets(8, 0, 0, 0)
          }

          hbox() {
            spacing = 16
            checkbox(readOnly) {
              text = "Read-only"
            }
            checkbox(hidden) {
              text = "Hidden"
            }
          }

          checkbox(recursive) {
            text = "Apply to subdirectories"
            padding = Insets(6, 0, 0, 0)
          }

          hbox() {
            spacing = 10
            padding = Insets(10, 0, 0, 0)
            alignment = Pos.CENTER_RIGHT

            button(applyButton) {
              text = "Apply"
              onAction = _ => {
                new Thread(() => {
                  applyAttributes()
                  ()
                }).start()
                close()
              }
            }
          }

          Seq(readOnly, hidden).foreach(installMixedFix)
        }

        if files.size == 1 then
          initSingle()
        else
          initMultiple()
      }
    }
  }

  override def build(): Window[Unit] = node
}

object WindowsPropertiesDialog {

  def apply[T](files: Seq[String])(body: (WindowsPropertiesDialog, BuildContext) ?=> Unit)
              (using ctx: BuildContext, parent: ElementBuilder[?]): Window[Unit] =
    DSL.create[Window[Unit], WindowsPropertiesDialog](Ref(), new WindowsPropertiesDialog(files))(body)
}

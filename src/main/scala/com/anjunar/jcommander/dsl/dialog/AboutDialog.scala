package com.anjunar.jcommander.dsl.dialog

import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.traits.HasSpacing.alignment
import com.anjunar.javafx.dsl.traits.HasText.text
import com.anjunar.javafx.dsl.traits.IsNode.vgrow
import com.anjunar.javafx.dsl.{ElementBuilder, Producer}
import com.anjunar.javafx.scene.control.label
import com.anjunar.javafx.scene.layout.vbox
import com.anjunar.javafx.scene.{header, window}
import com.anjunar.javafx.stage.Window
import javafx.geometry.Pos
import javafx.scene.layout.Priority

class AboutDialog extends ElementBuilder[Window[Unit]] {

  lazy val node : Window[Unit] = component[Window[Unit]] {
    window[Unit](300, 200) {
      
      header() {
        label() {
          text = "About"
        }
      }
      
      vbox() {
        vgrow = Priority.ALWAYS
        alignment = Pos.CENTER
        label() {
          text = "JCommander v1.0.1"
        }
      }
    }
  }

  override def build(): Window[Unit] = node

}

object AboutDialog extends Producer[AboutDialog, Window[Unit]]{
  override def createBuilder: AboutDialog = new AboutDialog()
}

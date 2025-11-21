package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.{ElementBuilder, NodeBuilder, Producer, Ref}
import com.anjunar.javafx.dsl.DSL.*
import javafx.scene.layout.{Priority, VBox}
import com.anjunar.jcommander.dsl.DriveButtons.*
import com.anjunar.jcommander.dsl.LocalFileTable.HastLocalFileTable.*

import java.io.File

class FilePane extends NodeBuilder[VBox] {

  private val fileTableRef = Ref[LocalFileTable]()

  override val node: VBox = component[VBox] {
    vbox() {

      DriveButtons() {
        change = (file : File) => {
          fileTableRef {
            directory = file.getAbsolutePath
          }
        }
        unmount = drive => {

        }
      }

      LocalFileTable(fileTableRef) {
        vgrow = Priority.ALWAYS
        directory = System.getProperty("user.home")
      }

    }
  }

  override def build(): VBox = node

}

object FilePane extends Producer[FilePane, VBox] {
  override def createBuilder: FilePane = new FilePane
}

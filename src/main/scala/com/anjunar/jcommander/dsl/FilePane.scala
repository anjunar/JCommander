package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.{ChildNodeBuilder, ElementBuilder, NodeBuilder, Producer, Ref}
import com.anjunar.jcommander.dsl.DriveButtons.*
import com.anjunar.jcommander.dsl.LocalFileTable.HastLocalFileTable.*
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.scene.layout.{Priority, VBox}

import java.io.File

class FilePane extends NodeBuilder[VBox] {

  private val fileTableRef = Ref[LocalFileTable]()

  private var onTableChange : FileTable => Unit = FileTable => {}

  lazy val node : VBox = {
    val filePane = component[VBox] {
      vbox() {
        vgrow = Priority.ALWAYS
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
    
    filePane
  }

  override def build(): VBox = node

}

object FilePane extends Producer[FilePane, VBox] {

  override def createBuilder: FilePane = new FilePane

  object HastLocalFileTable {

    def onTableChange()(using h: FilePane) : FileTable => Unit = h.onTableChange
    def onTableChange_=(v: FileTable => Unit)(using h: FilePane) : Unit = h.onTableChange = v

  }

}

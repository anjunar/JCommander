package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.traits.IsNode.vgrow
import com.anjunar.javafx.dsl.{BuildContext, ChildNodeBuilder, ElementBuilder, NodeBuilder, Producer, Ref}
import com.anjunar.javafx.scene.layout.vbox
import com.anjunar.jcommander.dsl.DriveButtons.*
import com.anjunar.jcommander.dsl.LocalFileTable.*
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.scene.layout.{Priority, VBox}

import java.io.File
import scala.compiletime.uninitialized

class FilePane extends NodeBuilder[VBox] {

  private val fileTableRef = Ref[LocalFileTable]()

  private var onTableChange : FileTable => Unit = uninitialized

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
            fileTableRef {
              directory = System.getProperty("user.home")
            }
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

  override def afterBuild(): Unit = onTableChange(fileTableRef.get)

}

object FilePane extends Producer[FilePane, VBox] {

  override def createBuilder: FilePane = new FilePane

  def onTableChange()(using h: FilePane & ElementBuilder[?], ctx : BuildContext): FileTable => Unit =
    h.read(h.onTableChange)

  def onTableChange_=(v: FileTable => Unit)(using h: FilePane & ElementBuilder[?], ctx : BuildContext): Unit =
    h.write(() => h.onTableChange = v)

}

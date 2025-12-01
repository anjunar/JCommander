package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.ChildBuilder.reactTo
import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.traits.HasHeight.{maxHeight, minHeight, prefHeight}
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasPadding
import com.anjunar.javafx.dsl.traits.HasText.text
import com.anjunar.javafx.dsl.traits.HasSpacing.spacing
import com.anjunar.javafx.dsl.traits.HasStyle.style
import com.anjunar.javafx.dsl.{BuildContext, ChildBuilder, ElementBuilder, NodeBuilder, Producer, Ref}
import com.anjunar.javafx.scene.control.button
import com.anjunar.javafx.scene.layout.hbox
import com.anjunar.jcommander.dsl.traits.HasDirectory
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.Node
import javafx.scene.layout.HBox
import org.apache.commons.vfs2.FileObject

import scala.compiletime.uninitialized

class BreadCrumb extends NodeBuilder[HBox], HasDirectory, HasPadding {

  private var table : Ref[? <: FileTable] = uninitialized

  private val crumbs = FXCollections.observableArrayList[button]()

  val directoryProperty = new SimpleStringProperty("")

  override lazy val node: HBox = component[HBox] {
    hbox() {
      style = "-fx-background-color: -fx-table-cell-border-color; -fx-alignment: CENTER_LEFT;"
      minHeight = 14
      prefHeight = 14
      maxHeight = 14
      spacing = 2

      reactTo(crumbs)
    }
  }

  private def getVfsPathParts(file: FileObject): Seq[FileObject] = {
    var parts = List(file)
    var parent = file.getParent

    while (parent != null) {
      parts = parent :: parts
      parent = parent.getParent
    }

    parts
  }

  def updateBreadcrumb(dir: String): Unit = {
    crumbs.clear()
    val current: FileObject = table.get.manager.resolveFile(dir)
    val parts = getVfsPathParts(current)

    for (fileObj <- parts) {
      val label =
        if (fileObj.getName.getBaseName.isEmpty)
          fileObj.getName.getRoot.getFriendlyURI
        else
          fileObj.getName.getBaseName

      val b = button.build() {
        text = label.replaceFirst("file:///", "").replaceAll("/", "") + "/"
        style =
          "-fx-background-color: transparent;" +
            "-fx-border-width: 0;" +
            "-fx-padding: 0;" +
            "-fx-text-fill: -fx-text-base-color;" +
            "-fx-opacity: 0.85;"
        minHeight = 14
        prefHeight = 14
        maxHeight = 14
        onAction = _ => table.get.loadDirectory(fileObj.getName.getURI)
      }

      crumbs.add(b)
    }
  }

  override def build(): HBox = node

  override def afterBuild(): Unit = {
    updateBreadcrumb(directoryProperty.get())
    directoryProperty.addListener((_, _, newValue) => updateBreadcrumb(newValue))
  }

}

object BreadCrumb extends Producer[BreadCrumb, HBox] {
  override def createBuilder: BreadCrumb = new BreadCrumb()

  def table()(using b: BreadCrumb & ElementBuilder[?], ctx : BuildContext): Ref[? <: FileTable] =
    b.read(b.read(b.table))
  def table_=(v: Ref[? <: FileTable])(using b: BreadCrumb & ElementBuilder[?], ctx : BuildContext): Unit =
    b.write(() => b.table = v)

}

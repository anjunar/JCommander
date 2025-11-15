package com.anjunar.jcommander.manager

import com.anjunar.jcommander.CdiUtils.*
import com.anjunar.jcommander.components.{AbstractFileTableComponent, LocalFileTableComponent}
import com.anjunar.jcommander.configuration.FileTableConf
import jakarta.enterprise.context.ApplicationScoped

import scala.compiletime.uninitialized

@ApplicationScoped
class FileTableManager {

  val leftConf = inject(classOf[FileTableConf.Left])
  val rightConf = inject(classOf[FileTableConf.Right])

  var source : AbstractFileTableComponent = uninitialized
  var target : AbstractFileTableComponent = uninitialized

  var left : AbstractFileTableComponent = new LocalFileTableComponent()
  var right : AbstractFileTableComponent = new LocalFileTableComponent()

  def loadLeft(table : AbstractFileTableComponent): Unit = {
    left = table
    left.node.requestFocus()
    source = table

    table.loadDirectory(leftConf.file.getAbsolutePath)

    table.node.focusedProperty().addListener((_, _, newValue) => {
      if (newValue) {
        val source = this.source
        val target = this.target

        if (source != table)
        this.source = table
        this.target = source
      }
    })
  }

  def loadRight(table: AbstractFileTableComponent): Unit = {
    right = table
    right.node.requestFocus()
    source = table

    table.loadDirectory(rightConf.file.getAbsolutePath)

    table.node.focusedProperty().addListener((_, _, newValue) => {
      if (newValue) {
        val source = this.source
        val target = this.target

        if (source != table) {
          this.source = target
          this.target = source
        }
      }
    })
  }

}

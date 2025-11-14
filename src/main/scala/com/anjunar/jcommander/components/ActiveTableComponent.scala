package com.anjunar.jcommander.components

import com.anjunar.jcommander.CdiUtils.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes

import java.nio.file.{FileStore, FileSystems}

@ApplicationScoped
class ActiveTableComponent {

  val leftTable: FileTableComponent.Left = inject(classOf[FileTableComponent.Left])
  val rightTable: FileTableComponent.Right = inject(classOf[FileTableComponent.Right])

  var active: FileTableComponent = leftTable
  var inActive: FileTableComponent = rightTable

  def setActive(table: FileTableComponent) : Unit = {
    if (table != active) {
      swap()
    }
  }

  def swap() : Unit = {
    val activeTable = active
    val inActiveTable = inActive

    active = inActiveTable
    inActive = activeTable

    inActive.node.selectionModel.value.clearSelection()

    val lastSelection = active.lastSelections.get(active.directory.getAbsolutePath)
    if (lastSelection.isDefined) {
      val fileItem = active.node.items.value.stream().filter(item => item.name == lastSelection.get).findFirst().get()
      active.node.selectionModel.value.select(fileItem)
    }

    active.node.requestFocus()
    
  }

}

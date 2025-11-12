package com.anjunar.jcommander.components

import com.anjunar.jcommander.components.DriveButtons.OnDriveChange
import com.anjunar.jcommander.inject
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes

import java.nio.file.{FileStore, FileSystems}

@ApplicationScoped
class ActiveTable {

  val leftTable: FileTable.Left = inject(classOf[FileTable.Left])
  val rightTable: FileTable.Right = inject(classOf[FileTable.Right])

  var active: FileTable = leftTable
  var inActive: FileTable = rightTable

  def setActive(table: FileTable) : Unit = {
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

  def onFileStoreChange(@Observes event: OnDriveChange) : Unit = {
    active.loadDirectory(event.file)
  }

}

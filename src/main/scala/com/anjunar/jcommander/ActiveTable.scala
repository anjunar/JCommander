package com.anjunar.jcommander

import jakarta.enterprise.context.ApplicationScoped

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

    active.node.requestFocus()
  }

}

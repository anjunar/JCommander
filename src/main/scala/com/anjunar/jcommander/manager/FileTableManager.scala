package com.anjunar.jcommander.manager

import com.anjunar.jcommander.components.LocalFileTableComponent
import com.anjunar.jcommander.configuration.FileTableConf
import com.anjunar.jcommander.dsl.FileTable
import com.anjunar.jcommander.utils.CdiUtils.*
import jakarta.enterprise.context.ApplicationScoped
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.EventHandler
import javafx.scene.input.{KeyCode, KeyEvent, MouseEvent}

import scala.compiletime.uninitialized

@ApplicationScoped
class FileTableManager {

  var source: FileTable = uninitialized
  var target: FileTable = uninitialized

  var left: FileTable = uninitialized
  var right: FileTable = uninitialized

  private var leftFocusListener: ChangeListener[java.lang.Boolean] = uninitialized
  private var rightFocusListener: ChangeListener[java.lang.Boolean] = uninitialized

  private var leftKeyHandler: EventHandler[KeyEvent] = uninitialized
  private var rightKeyHandler: EventHandler[KeyEvent] = uninitialized


  private def createTabSwitchHandler: EventHandler[KeyEvent] = {

    (event: KeyEvent) => {
      if (event.getCode == KeyCode.TAB) {
        event.consume()

        // Aktuelle Werte zwischenspeichern
        val oldSource = source
        val oldTarget = target

        source = oldTarget
        target = oldSource

        target.node.getSelectionModel.clearSelection()

        val newSource = source
        newSource.node.requestFocus()

        val lastSelectionName = newSource.lastSelections(newSource.directory)

        val itemOpt = newSource.node.getItems.stream()
          .filter(item => item.file == lastSelectionName)
          .findFirst()

        val itemToSelect =
          if (itemOpt.isPresent) itemOpt.get()
          else newSource.node.getItems.get(0)

        newSource.node.getSelectionModel.select(itemToSelect)
      }
    }
  }


  def loadLeft(table: FileTable, loadAsTarget : Boolean = false): Unit = {

    if (left != null && leftFocusListener != null)
      left.node.focusedProperty().removeListener(leftFocusListener)

    if (left != null && leftKeyHandler != null)
      left.node.removeEventHandler(KeyEvent.KEY_PRESSED, leftKeyHandler)

    left = table
    left.node.requestFocus()
    
    if (loadAsTarget) {
      target = table
      source = right
    } else {
      source = table
      target = right
    }

    leftFocusListener = (o, old, newValue) => {
      if (newValue) {
        val currentSource = FileTableManager.this.source
        if (currentSource ne table) {
          FileTableManager.this.target = currentSource
          FileTableManager.this.target.node.getSelectionModel.clearSelection()
          FileTableManager.this.source = table
        }
      }
    }

    table.node.focusedProperty().addListener(leftFocusListener)

    leftKeyHandler = createTabSwitchHandler
    table.node.addEventHandler(KeyEvent.KEY_PRESSED, leftKeyHandler)
  }



  def loadRight(table: FileTable, loadAsTarget : Boolean = false): Unit = {

    if (right != null && rightFocusListener != null)
      right.node.focusedProperty().removeListener(rightFocusListener)

    if (right != null && rightKeyHandler != null)
      right.node.removeEventHandler(KeyEvent.KEY_PRESSED, rightKeyHandler)

    right = table
    right.node.requestFocus()
    
    if (loadAsTarget) {
      target = table
      source = left
    } else {
      source = table
      target = left
    }
    
    rightFocusListener = (o, old, newValue) => {
      if (newValue) {
        val currentSource = FileTableManager.this.source
        if (currentSource ne table) {
          FileTableManager.this.target = currentSource
          FileTableManager.this.target.node.getSelectionModel.clearSelection()
          FileTableManager.this.source = table
        }
      }
    }

    table.node.focusedProperty().addListener(rightFocusListener)

    rightKeyHandler = createTabSwitchHandler
    table.node.addEventHandler(KeyEvent.KEY_PRESSED, rightKeyHandler)
  }
}

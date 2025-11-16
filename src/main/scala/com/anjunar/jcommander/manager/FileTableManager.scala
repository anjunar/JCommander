package com.anjunar.jcommander.manager

import com.anjunar.jcommander.utils.CdiUtils.*
import com.anjunar.jcommander.components.{AbstractFileTableComponent, LocalFileTableComponent}
import com.anjunar.jcommander.configuration.FileTableConf
import jakarta.enterprise.context.ApplicationScoped
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.EventHandler
import javafx.scene.input.{KeyCode, KeyEvent}

import scala.compiletime.uninitialized

@ApplicationScoped
class FileTableManager {

  val leftConf = inject(classOf[FileTableConf.Left])
  val rightConf = inject(classOf[FileTableConf.Right])

  var _source: AbstractFileTableComponent = uninitialized
  var _target: AbstractFileTableComponent = uninitialized

  def source = _source
  def target = _target
  def source_=(value: AbstractFileTableComponent): Unit = _source = {
    println(s"Source changed to ${value}")
    value
  }
  def target_=(value: AbstractFileTableComponent): Unit = _target = {
    println(s"Target changed to ${value}")
    value
  }

  var left: AbstractFileTableComponent = uninitialized
  var right: AbstractFileTableComponent = uninitialized

  var leftFocusListener: ChangeListener[java.lang.Boolean] = uninitialized
  var rightFocusListener: ChangeListener[java.lang.Boolean] = uninitialized

  var leftKeyHandler: EventHandler[KeyEvent] = uninitialized
  var rightKeyHandler: EventHandler[KeyEvent] = uninitialized


  private def createTabSwitchHandler: EventHandler[KeyEvent] = {

    (event: KeyEvent) => {
      if (event.getCode == KeyCode.TAB) {
        event.consume()

        // Aktuelle Werte zwischenspeichern
        val oldSource = source
        val oldTarget = target

        source = oldTarget
        target = oldSource

        target.node.selectionModel.value.clearSelection()

        val newSource = source
        newSource.node.requestFocus()

        val lastSelectionName = newSource.lastSelections(newSource.directory)

        val itemOpt = newSource.node.items.value.stream()
          .filter(item => item.name == lastSelectionName)
          .findFirst()

        val itemToSelect =
          if (itemOpt.isPresent) itemOpt.get()
          else newSource.node.items.value.get(0)

        newSource.node.selectionModel.value.select(itemToSelect)
      }
    }
  }


  def loadLeft(table: AbstractFileTableComponent, loadAsTarget : Boolean = false): Unit = {

    if (left != null && leftFocusListener != null)
      left.node.focusedProperty().removeListener(leftFocusListener)

    if (left != null && leftKeyHandler != null)
      left.node.removeEventHandler(KeyEvent.KEY_PRESSED, leftKeyHandler)

    left = table
    left.node.requestFocus()
    
    if (loadAsTarget) {
      target = table
    } else {
      source = table
    }

    if (table.isInstanceOf[LocalFileTableComponent])
      table.loadDirectory(leftConf.file.getAbsolutePath)

    leftFocusListener = (o, old, newValue) => {
      if (newValue) {
        val currentSource = FileTableManager.this.source
        if (currentSource ne table) {
          FileTableManager.this.target = currentSource
          FileTableManager.this.target.node.selectionModel.value.clearSelection()
          FileTableManager.this.source = table
        }
      }
    }

    table.node.focusedProperty().addListener(leftFocusListener)

    leftKeyHandler = createTabSwitchHandler
    table.node.addEventHandler(KeyEvent.KEY_PRESSED, leftKeyHandler)
  }



  def loadRight(table: AbstractFileTableComponent, loadAsTarget : Boolean = false): Unit = {

    if (right != null && rightFocusListener != null)
      right.node.focusedProperty().removeListener(rightFocusListener)

    if (right != null && rightKeyHandler != null)
      right.node.removeEventHandler(KeyEvent.KEY_PRESSED, rightKeyHandler)

    right = table
    right.node.requestFocus()
    
    if (loadAsTarget) {
      target = table
    } else {
      source = table
    }
    
    if (table.isInstanceOf[LocalFileTableComponent])
      table.loadDirectory(rightConf.file.getAbsolutePath)

    rightFocusListener = (o, old, newValue) => {
      if (newValue) {
        val currentSource = FileTableManager.this.source
        if (currentSource ne table) {
          FileTableManager.this.target = currentSource
          FileTableManager.this.target.node.selectionModel.value.clearSelection()
          FileTableManager.this.source = table
        }
      }
    }

    table.node.focusedProperty().addListener(rightFocusListener)

    rightKeyHandler = createTabSwitchHandler
    table.node.addEventHandler(KeyEvent.KEY_PRESSED, rightKeyHandler)
  }
}

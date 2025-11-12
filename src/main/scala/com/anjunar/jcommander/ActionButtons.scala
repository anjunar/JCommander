package com.anjunar.jcommander

import com.anjunar.jcommander.files.FileUtils
import com.typesafe.scalalogging.Logger
import javafx.concurrent
import javafx.concurrent.WorkerStateEvent
import javafx.event.{Event, EventHandler}
import scalafx.Includes.jfxDialogPane2sfx
import scalafx.application.Platform
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.concurrent.Task
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, ButtonType, Dialog, ProgressBar}
import scalafx.scene.layout.{HBox, Priority, VBox}

import java.nio.file.{Files, Path, StandardCopyOption}
import scala.jdk.CollectionConverters.*

class ActionButtons extends HBox {

  val log = Logger[ActionButtons]
  
  val fileUtils = inject(classOf[FileUtils])
  
  val activeTable = inject(classOf[ActiveTable])
  
  val toggleTheme = inject(classOf[DarkMode])

  spacing = 2
  fillHeight = true
  maxWidth = Double.MaxValue

  val buttons =  Seq(
    new Button() {
      text = "F1 Help"
      onMouseClicked = _ => {
        
      }
    },
    new Button() {
      text = "F2 Rename"
      onMouseClicked = _ => {
        fileUtils.renameFile(activeTable.active)
      }
    },
    new Button() {
      text = "F3 View"
      onMouseClicked = _ => {

      }
    },
    new Button() {
      text = "F4 Edit"
      onMouseClicked = _ => {

      }
    },
    new Button() {
      text = "F5 Copy"
      onMouseClicked = _ => {
        fileUtils.copyFiles(activeTable.active, activeTable.inActive)
      }
    },
    new Button() {
      text = "F6 Move"
      onMouseClicked = _ => {
        fileUtils.moveFiles(activeTable.active, activeTable.inActive)
      }
    },
    new Button() {
      text = "F7 MkDir"
      onMouseClicked = _ => {
        fileUtils.mkDir(activeTable.active)
      }
    },
    new Button() {
      text = "F8 Delete"
      onMouseClicked = _ => {
        fileUtils.deleteFiles(activeTable.active, activeTable.inActive)
      }
    },
    new Button() {
      text = "F9 Menu"
      onMouseClicked = _ => {
        
      }
    },
    new Button() {
      text = "F10 Quit"
      onMouseClicked = _ => {
        
      }
    },
    toggleTheme.node
  )

  buttons.foreach { b =>
    b.maxWidth = Double.MaxValue
    HBox.setHgrow(b, Priority.Always)
  }

  children = buttons


}

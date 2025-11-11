package com.anjunar.jcommander

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

class ActionButtons(toggleTheme : Button, function : String => Unit) extends HBox {

  val log = Logger[ActionButtons]

  spacing = 2
  fillHeight = true
  maxWidth = Double.MaxValue

  val buttons =  Seq(
    new Button() {
      text = "F1 Help"
      onMouseClicked = _ => {
        function("F1")
      }
    },
    new Button() {
      text = "F2 Rename"
      onMouseClicked = _ => {
        function("F2")
      }
    },
    new Button() {
      text = "F3 View"
      onMouseClicked = _ => {
        function("F3")
      }
    },
    new Button() {
      text = "F4 Edit"
      onMouseClicked = _ => {
        function("F4")
      }
    },
    new Button() {
      text = "F5 Copy"
      onMouseClicked = _ => {
        function("F5")
      }
    },
    new Button() {
      text = "F6 Move"
      onMouseClicked = _ => {
        function("F6")
      }
    },
    new Button() {
      text = "F7 MkDir"
      onMouseClicked = _ => {
        function("F7")
      }
    },
    new Button() {
      text = "F8 Delete"
      onMouseClicked = _ => {
        function("F8")
      }
    },
    new Button() {
      text = "F9 Menu"
      onMouseClicked = _ => {
        function("F9")
      }
    },
    new Button() {
      text = "F10 Quit"
      onMouseClicked = _ => {
        function("F10")
      }
    },
    toggleTheme
  )

  buttons.foreach { b =>
    b.maxWidth = Double.MaxValue
    HBox.setHgrow(b, Priority.Always)
  }

  children = buttons


}

package com.anjunar.jcommander

import com.typesafe.scalalogging.Logger
import javafx.concurrent
import javafx.concurrent.WorkerStateEvent
import javafx.event.{Event, EventHandler}
import scalafx.Includes.jfxDialogPane2sfx
import scalafx.application.Platform
import scalafx.beans.property.ObjectProperty
import scalafx.concurrent.Task
import scalafx.scene.control.{Button, ButtonType, Dialog, ProgressBar}
import scalafx.scene.layout.{HBox, Priority, VBox}

import java.nio.file.{Files, Path, StandardCopyOption}
import scala.jdk.CollectionConverters.*

class ActionButtons(toggleTheme : Button, activeTable : ObjectProperty[FileTable], otherTable : ObjectProperty[FileTable]) extends HBox {

  val log = Logger[ActionButtons]

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
      text = "F2 User Menu"
    },
    new Button() {
      text = "F3 View"
    },
    new Button() {
      text = "F4 Edit"
    },
    new Button() {
      text = "F5 Copy"
    },
    new Button() {
      text = "F6 Move"
      onMouseClicked = _ => {

        val confirmDialog = new Dialog[ButtonType]() {
          title = "Move Files"
          headerText = "Sollen die ausgewählten Dateien verschoben werden?"
          dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
        }

        confirmDialog.resultConverter = btn => btn

        // WICHTIG: showAndWait() – aber danach sofort weiter!
        confirmDialog.showAndWait().foreach { result =>
          if (result == ButtonType.OK) {

            // --- ALLES AB HIER IM UI-THREAD ---
            val selectedItems = activeTable.value.selectionModel.value.getSelectedItems
            val allFiles = selectedItems.stream().flatMap { fileItem =>
              val path = fileItem.file.toPath
              if (Files.isDirectory(path)) Files.walk(path)
              else java.util.stream.Stream.of(path)
            }.toList.asScala.toSeq

            val progressBar = new ProgressBar { prefWidth = 350 }

            // WICHTIG: initOwner(null) → KEIN Elternfenster!
            val progressDialog = new Dialog[Unit]() {
              title = "Moving Files..."
              initOwner(null)  // ← KRITISCH!
              dialogPane().content = new VBox(10, progressBar)
              dialogPane().buttonTypes = Seq()
            }

            val task = new concurrent.Task[Unit]() {
              override def call(): Unit = {
                val total = allFiles.size
                allFiles.zipWithIndex.foreach { case (path, i) =>
                  if (isCancelled) return
                  val target = otherTable.value.directory.toPath.resolve(path.getFileName)
                  Files.move(path, target, StandardCopyOption.REPLACE_EXISTING)
                  updateProgress(i + 1, total)
                }
              }
            }

            progressBar.progress <== task.progressProperty()

            // --- TASK EVENTS ---
            task.setOnSucceeded { _ =>
              Platform.runLater {
                if (progressDialog.isShowing) {
                  log.info("SUCCESS: Schließe ProgressDialog")
                  progressDialog.close()
                }
                activeTable.value.refresh()
                otherTable.value.refresh()
              }
            }

            task.setOnFailed { e =>
              Platform.runLater {
                log.error("FAILED", e.getSource.getException)
                if (progressDialog.isShowing) {
                  progressDialog.close()
                }
              }
            }

            // --- STARTE ALLES ---
            progressDialog.show()        // NICHT showAndWait()!
            new Thread(task).start()     // Task im Hintergrund
          }
        }
      }
    },
    new Button() {
      text = "F7 MkDir"
    },
    new Button() {
      text = "F8 Delete"
    },
    new Button() {
      text = "F9 Menu"
    },
    new Button() {
      text = "F10 Quit"
    },
    toggleTheme
  )

  buttons.foreach { b =>
    b.maxWidth = Double.MaxValue
    HBox.setHgrow(b, Priority.Always)
  }

  children = buttons


}

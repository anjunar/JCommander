package com.anjunar.jcommander

import scalafx.application.Platform
import scalafx.scene.Node
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.HBox

import java.nio.file.{FileStore, FileSystems}
import scala.jdk.CollectionConverters.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.mutable
import java.util.concurrent.atomic.AtomicBoolean

class DriveButtons(load: FileStore => Unit, pollIntervalMillis: Long = 3000) extends HBox {

  private val running = new AtomicBoolean(true)
  private var lastDrives: Set[String] = currentDriveNames

  val selectedLabel = new Label("Kein Laufwerk ausgewählt")

  spacing = 10

  refreshButtons()

  private val watcherThread = Future {
    while (running.get()) {
      Thread.sleep(pollIntervalMillis)
      val now = currentDriveNames
      if (now != lastDrives) {
        lastDrives = now
        Platform.runLater(() => refreshButtons())
      }
    }
  }

  private def currentDriveNames: Set[String] =
    FileSystems.getDefault.getFileStores.iterator().asScala.map(_.name()).toSet

  private def refreshButtons(): Unit = {
    val stores = FileSystems.getDefault.getFileStores.iterator().asScala.toSeq
    children.clear()

    val buttons : Seq[Node] = stores.map { store =>
      val name = Option(store.name()).getOrElse("Unbenannt")
      val displayName = if (name.nonEmpty) name else store.toString
      new Button(displayName) {
        onAction = _ => {
          selectedLabel.text = s"Ausgewählt: $displayName"
          load(store)
        }
      }
    }

    children = buttons
  }

  def stop(): Unit = {
    running.set(false)
  }
}

package com.anjunar.jcommander.components

import com.anjunar.jcommander.manager.{Drive, DriveDetectionService, FileManager}
import com.anjunar.jcommander.utils.CdiUtils.*
import com.typesafe.scalalogging.Logger
import scalafx.application.Platform
import scalafx.embed.swing.SwingFXUtils
import scalafx.scene.Node
import scalafx.scene.control.{Button, Label}
import scalafx.scene.image.ImageView
import scalafx.scene.layout.HBox

import java.io.{BufferedReader, File, InputStreamReader}
import java.nio.file.{FileStore, FileSystems, Files}
import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.jdk.CollectionConverters.*

class DriveButtonsComponent(change: File => Unit, unmount: Drive => Unit) extends Component[HBox] {

  private val log = Logger[DriveButtonsComponent]
  private val running = new AtomicBoolean(true)
  private val fileUtils = inject(classOf[FileManager])
  private val pollIntervalMillis = 3000
  private val driveService = new DriveDetectionService

  val selectedLabel = new Label("Kein Laufwerk ausgewählt")

  val node = new HBox {
    style = "-fx-padding: 0 0 0 5 ;"
    spacing = 10
  }

  val home = new Button("Home") {
    graphic = new ImageView {
      fitWidth = 16
      fitHeight = 16
      preserveRatio = true
      image = SwingFXUtils.toFXImage(
        fileUtils.getFileIcon(new File(System.getProperty("user.home")).getAbsolutePath, false),
        null
      )
    }
    style = "-fx-background-color: transparent;" +
      "-fx-border-color: transparent;" +
      "-fx-padding: 0;" +
      "-fx-focus-color: transparent;" +
      "-fx-faint-focus-color: transparent;"
    onAction = _ => change(new File(System.getProperty("user.home")))
  }

  private var lastDrives: Seq[Drive] = driveService.listDrives()

  refreshButtons()

  // ------------------------------
  // Watcher-Thread
  // ------------------------------
  private val watcherThread = Future {
    while (running.get()) {
      try {
        Thread.sleep(pollIntervalMillis)
        val now = driveService.listDrives()
        if (now != lastDrives) {
          val difference = lastDrives.diff(now)
          difference.foreach(unmount)
          lastDrives = now
          Platform.runLater(() => refreshButtons())
        }
      } catch {
        case ex: Exception => log.error(ex.getMessage, ex)
      }
    }
  }

  private def refreshButtons(): Unit = {
    node.children.clear()
    val drives = driveService.listDrives().sortBy(_.file.getAbsolutePath)
    
    val buttons: Seq[Node] = drives.map { drive =>
      val name = drive.name
      new Button(name) {
        graphic = new ImageView {
          fitWidth = 16
          fitHeight = 16
          preserveRatio = true
          image = SwingFXUtils.toFXImage(
            fileUtils.getFileIcon(drive.file.getAbsolutePath, false),
            null
          )
        }
        style =
          "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;" +
            "-fx-padding: 0;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;"
        onAction = _ => {
          if (drive.mounted) change(drive.file)
          else {
            driveService.mountDrive(drive) match {
              case Some(updated) =>
                change(updated.file)
                refreshButtons()
              case None =>
                selectedLabel.text = s"Mount error: ${drive.device.getOrElse(drive.name)}"
            }
          }
        }
      }
    }

    node.children = Seq(home) ++ buttons
  }

  def stop(): Unit = running.set(false)
}

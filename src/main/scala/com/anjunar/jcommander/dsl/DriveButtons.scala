package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.{BuildContext, ChildBuilder, ElementBuilder, Producer, Ref}
import com.anjunar.javafx.scene.control.button
import com.anjunar.javafx.scene.control.label
import com.anjunar.javafx.scene.image.ImageView
import com.anjunar.javafx.scene.image.ImageView.*
import com.anjunar.jcommander.manager.{Drive, DriveDetectionService, FileManager}
import com.anjunar.jcommander.utils.CdiUtils.*
import com.typesafe.scalalogging.Logger
import javafx.scene.Node
import javafx.scene.layout.HBox
import scalafx.application.Platform
import scalafx.embed.swing.SwingFXUtils

import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import scala.compiletime.uninitialized
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class DriveButtons extends ElementBuilder[HBox] {

  private val log = Logger[DriveButtons]
  private val running = new AtomicBoolean(true)
  private val fileUtils = inject(classOf[FileManager])
  private val driveService = new DriveDetectionService
  private val pollIntervalMillis = 3000

  private val selectedRef = Ref[label]()
  private var lastDrives: Seq[Drive] = driveService.listDrives()

  var change: File => Unit = uninitialized
  var unmount: Drive => Unit = uninitialized

  val node: HBox = component[HBox] {
    hbox() {
      spacing = 10
      style = "-fx-padding: 0 0 0 5;"
    }
  }

  private def homeButton(): Node =
    component[Node] {
      button() {
        graphic = ImageView() {
          fitWidth = 16
          fitHeight = 16
          image = SwingFXUtils.toFXImage(
            fileUtils.getFileIcon(System.getProperty("user.home"), false),
            null
          )
        }

        style =
          "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;" +
            "-fx-padding: 0;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;"

        onAction = _ => change(new File(System.getProperty("user.home")))
      }
    }

  private def driveButton(drive: Drive): Node =
    component[Node] {
      button() {
        text = drive.name

        graphic = ImageView() {
          fitWidth = 16
          fitHeight = 16
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
          if (drive.mounted)
            change(drive.file)
          else
            driveService.mountDrive(drive) match {
              case Some(updated) =>
                change(updated.file)
                refreshButtons()
              case None =>
                selectedRef {
                  text = s"Mount error: ${drive.device.getOrElse(drive.name)}"
                }
            }
        }
      }
    }

  private def refreshButtons(): Unit = {
    val drives = driveService.listDrives().sortBy(_.file.getAbsolutePath)

    node.getChildren.clear()
    node.getChildren.add(homeButton())

    drives.foreach { d =>
      node.getChildren.add(driveButton(d))
    }
  }

  refreshButtons()

  private val watcherThread = Future {
    while (running.get()) {
      try {
        Thread.sleep(pollIntervalMillis)
        val now = driveService.listDrives()
        if (now != lastDrives) {
          val diff = lastDrives.diff(now)
          diff.foreach(unmount)
          lastDrives = now
          Platform.runLater(() => refreshButtons())
        }
      } catch {
        case ex: Exception => log.error(ex.getMessage, ex)
      }
    }
  }

  override def build(): HBox = node

  def stop(): Unit = running.set(false)
}

object DriveButtons extends Producer[DriveButtons, HBox] {

  override def createBuilder: DriveButtons = new DriveButtons()

  object HasDriveButtons {
    
    def change()(using d : DriveButtons) : File => Unit = d.change
    def change_=(v : File => Unit)(using d : DriveButtons) : Unit = d.change = v

    def unmount()(using d: DriveButtons): Drive => Unit = d.unmount
    def unmount_=(v: Drive => Unit)(using d: DriveButtons): Unit = d.unmount = v


  }
  
  export HasDriveButtons.*
    
}

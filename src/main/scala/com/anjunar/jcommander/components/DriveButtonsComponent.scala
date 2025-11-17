package com.anjunar.jcommander.components

import com.anjunar.jcommander.manager.FileManager
import com.anjunar.jcommander.utils.CdiUtils.*
import com.typesafe.scalalogging.Logger
import jakarta.enterprise.context.{ApplicationScoped, Dependent}
import jakarta.enterprise.event.Event
import jakarta.enterprise.inject.spi.BeanManager
import jakarta.inject.Inject
import scalafx.application.Platform
import scalafx.embed.swing.SwingFXUtils
import scalafx.scene.Node
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.HBox
import scalafx.Includes.*
import scalafx.scene.image.ImageView

import java.io.File
import java.nio.file.{FileStore, FileSystems}
import java.util.concurrent.atomic.AtomicBoolean
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters.*

class DriveButtonsComponent(change : File => Unit, unmount : File => Unit) extends Component[HBox] {

  private val log = Logger[DriveButtonsComponent]

  private var lastDrives: Set[File] = currentDriveNames

  private val running = new AtomicBoolean(true)
  private val fileUtils = inject(classOf[FileManager])

  private val pollIntervalMillis = 3000

  val selectedLabel = new Label("Kein Laufwerk ausgewählt")

  val node = new HBox {
    style = "-fx-padding: 0 0 0 5 ;"
    spacing = 10
  }

  val home = new Button("Home") {
    graphic = new ImageView(
      SwingFXUtils.toFXImage(
        fileUtils.getFileIcon(new File(System.getProperty("user.home")).getAbsolutePath, false),
        null
      )
    )
    style = "-fx-background-color: transparent;" +
      "-fx-border-color: transparent;" +
      "-fx-padding: 0;" +
      "-fx-focus-color: transparent;" +
      "-fx-faint-focus-color: transparent;"

    onAction = _ => {
      change(new File(System.getProperty("user.home")))
    }
  }

  refreshButtons()

  private val watcherThread = Future {
    while (running.get()) {
      try {
        Thread.sleep(pollIntervalMillis)
        val now = currentDriveNames
        if (now != lastDrives) {

          val difference = lastDrives.diff(now)

          difference.foreach(file => {
            unmount(file)
          })

          lastDrives = now
          Platform.runLater(() => refreshButtons())
        }
      } catch {
        case ex : Exception => log.error(ex.getMessage, ex)
      }
    }
  }

  private def currentDriveNames: Set[File] =
    FileSystems.getDefault.getFileStores.iterator().asScala.map(getFileStore).toSet

  private def refreshButtons(): Unit = {
    val stores = FileSystems.getDefault.getFileStores.iterator().asScala.toSeq
    node.children.clear()

    val buttons : Seq[Node] = stores.map { store =>
      val name = Option(store.name()).getOrElse("Unbenannt")
      val root = getFileStore(store)
      val displayName = if (name.nonEmpty) name else store.toString
      new Button(displayName) {
        graphic = new ImageView(
          SwingFXUtils.toFXImage(
            fileUtils.getFileIcon(root.getAbsolutePath, false),
            null
          )
        )
        style =
          "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;" +
            "-fx-padding: 0;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;"
        onAction = _ => {
          selectedLabel.text = s"Ausgewählt: $displayName"
          change(root)
        }
      }
    }

    node.children = Seq(home) ++ buttons
  }

  private def getFileStore(store: FileStore): File = {
    val roots = FileSystems.getDefault.getRootDirectories.iterator()
    while (roots.hasNext) {
      val root = roots.next()
      try {
        val rootStore = java.nio.file.Files.getFileStore(root)
        if (rootStore == store) {
          return root.toFile
        }
      } catch {
        case _: Exception => return null
      }
    }
    null
  }

  def stop(): Unit = {
    running.set(false)
  }
}
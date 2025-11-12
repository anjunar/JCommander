package com.anjunar.jcommander.components

import com.anjunar.jcommander.Component
import com.anjunar.jcommander.components.DriveButtons.OnDriveChange
import jakarta.enterprise.context.{ApplicationScoped, Dependent}
import jakarta.enterprise.event.Event
import jakarta.enterprise.inject.spi.BeanManager
import jakarta.inject.Inject
import scalafx.application.Platform
import scalafx.scene.Node
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.HBox

import java.io.File
import java.nio.file.{FileStore, FileSystems}
import java.util.concurrent.atomic.AtomicBoolean
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters.*

@Dependent
class DriveButtons extends Component[HBox] {

  private val running = new AtomicBoolean(true)
  private var lastDrives: Set[String] = currentDriveNames

  private val pollIntervalMillis = 3000

  val selectedLabel = new Label("Kein Laufwerk ausgewählt")

  @Inject
  var beanManager : BeanManager = uninitialized

  lazy val node = new HBox {
    spacing = 10
  }

  val home = new Button("Home") {
    onAction = _ => {
      beanManager.getEvent.fire(OnDriveChange(new File(System.getProperty("user.home"))))
    }
  }

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
    node.children.clear()

    val buttons : Seq[Node] = stores.map { store =>
      val name = Option(store.name()).getOrElse("Unbenannt")
      val displayName = if (name.nonEmpty) name else store.toString
      new Button(displayName) {
        onAction = _ => {
          selectedLabel.text = s"Ausgewählt: $displayName"
          val roots = FileSystems.getDefault.getRootDirectories.iterator()
          while (roots.hasNext) {
            val root = roots.next()
            try {
              val rootStore = java.nio.file.Files.getFileStore(root)
              if (rootStore == store) {
                beanManager.getEvent.fire(OnDriveChange(root.toFile))
              }
            } catch {
              case _: Exception =>
            }
          }
        }
      }
    }

    node.children = Seq(home) ++ buttons
  }

  def stop(): Unit = {
    running.set(false)
  }
}

object DriveButtons {

  case class OnDriveChange(file : File)

}

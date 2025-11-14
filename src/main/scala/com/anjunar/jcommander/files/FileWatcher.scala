package com.anjunar.jcommander.files

import com.anjunar.jcommander.components.FileTableComponent
import com.typesafe.scalalogging.Logger
import javafx.application.Platform
import scalafx.scene.control.TableView

import java.io.File
import java.nio.file.*
import java.nio.file.StandardWatchEventKinds.*
import scala.jdk.CollectionConverters.*

class FileWatcher(val path: Path, table: FileTableComponent) {
  private val log = Logger[FileWatcher]
  private val watcher = FileSystems.getDefault.newWatchService()
  path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)

  @volatile private var running = true

  private val thread = new Thread(() => {
    try {
      while (running) {
        val key = watcher.take()
        for (event <- key.pollEvents().asScala) {
          val kind = event.kind()
          if (kind != StandardWatchEventKinds.OVERFLOW) {
            val ev = event.asInstanceOf[WatchEvent[Path]]
            val changedFile = path.resolve(ev.context())
            Platform.runLater(() => {
              table.updateFile(changedFile, kind)
            })
          }
        }
        key.reset()
      }
    } catch {
      case _: InterruptedException =>
        log.info("FileWatcher interrupted")
    } finally watcher.close()
  }, s"watcher-${path.getFileName}")

  thread.setDaemon(true)
  def start(): Unit = { running = true; thread.start() }
  def stop(): Unit = { running = false; thread.interrupt() }
}

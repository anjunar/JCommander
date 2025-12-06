package com.anjunar.jcommander.files

import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.traits.HasGraphic.graphic
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasSpacing.{alignment, spacing, spacing_=}
import com.anjunar.javafx.dsl.traits.HasStyle.style
import com.anjunar.javafx.dsl.traits.HasText.text
import com.anjunar.javafx.dsl.{BuildContext, ElementBuilder}
import com.anjunar.javafx.scene.control.*
import com.anjunar.javafx.scene.layout.hbox
import com.anjunar.javafx.stage.Window
import com.anjunar.jcommander.commands.{DeleteCommand, RenameCommand}
import com.anjunar.jcommander.dsl.Icon
import com.anjunar.jcommander.dsl.Icon.{iconLiteral, iconSize}
import com.anjunar.jcommander.dsl.dialog.{UnixPropertiesDialog, WindowsPropertiesDialog}
import com.anjunar.jcommander.utils.CdiUtils.inject
import javafx.application.Platform
import javafx.event.{ActionEvent, EventHandler}
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.{ContextMenu, Menu, MenuItem}
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.sys.process.*

trait WinFallbackFileUtils {

  private var contextMenuOpen = false

  def ctxItem(text1: String, iconName: String)(action: EventHandler[ActionEvent])(using BuildContext, ElementBuilder[?]): MenuItem = {
    menuItem() {
      style = "-fx-padding: 4 10;"
      onAction = action
      graphic = hbox() {
        spacing = 10
        alignment = Pos.CENTER_LEFT
        style = "-fx-padding: 6 4 6 4;"
        Icon() {
          iconSize = 18
          iconLiteral = iconName
        }
        label() {
          style = "-fx-font-size: 13px;"
          text = text1
        }
      }
    }
  }

  def ctxSubMenu(text1: String, iconName: String, items: Seq[(BuildContext, ElementBuilder[Menu]) => MenuItem])(using BuildContext, ElementBuilder[?]): Menu = {
    menu() {
      graphic = component[HBox] {
        hbox() {
          spacing = 10
          alignment = Pos.CENTER_LEFT
          style = "-fx-padding: 6 4 6 4;"
          Icon() {
            iconSize = 18
            iconLiteral = iconName
          }
          label() {
            style = "-fx-font-size: 13px;"
            text = text1
          }
        }
      }
      items.foreach(item => item(summon[BuildContext], summon[ElementBuilder[Menu]]))
    }
  }

  def winFileContext(files: Seq[String], event: MouseEvent): Unit = {
    if files.isEmpty then return
    if contextMenuOpen then return

    val single = files.size == 1
    val fHead = files.head
    val parentDir = Path.of(fHead).getParent.toString

    val openWithApps = Seq("notepad.exe", "code", "wordpad.exe", "explorer.exe")

    val menu = component[ContextMenu] {
      contextMenu() {
        if single then ctxItem("Open", "mdi2f-file")(_ => {
          winExecuteFile(fHead)
        })
        ctxSubMenu(
          "Open With…",
          "mdi2o-open-in-new",
          openWithApps.map(app =>
            (elementBuilder, buildContext) => ctxItem(app, "mdi2a-application")(_ => {
              new Thread(() => {
                Seq("cmd", "/c", "start", "", app, fHead).!
                ()
              }).start()
            })(using elementBuilder, buildContext)
          )
        )
        separatorMenuItem() {}
        ctxItem("Copy", "mdi2c-content-copy")(_ => {
          FileClipboard.copyMany(files)
        })
        ctxItem("Paste", "mdi2c-content-paste")(_ => {
          FileClipboard.pasteToDirectory(parentDir)
        })
        if single then
          ctxItem("Rename", "mdi2r-rename-box")(_ => {
            val command = inject(classOf[RenameCommand])
            command.execute()
          })
        ctxItem("Duplicate", "mdi2c-content-copy")(_ => {
          new Thread(() => duplicateFiles(files)).start()
        })
        ctxItem("Delete", "mdi2d-delete")(_ => {
          val command = inject(classOf[DeleteCommand])
          command.execute()
        })
        separatorMenuItem() {}
        ctxSubMenu(
          "Compress",
          "mdi2z-zip-box",
          Seq(
            (elementBuilder, buildContext) => ctxItem("Create zip", "mdi2z-zip-box")(_ => {
              new Thread(() => {
                val dir = new File(files.head).getParentFile
                val zipPath = Paths.get(dir.getAbsolutePath, "archive.zip").toString
                val args = files.mkString("','")
                val script = s"Compress-Archive -LiteralPath '${args}' -DestinationPath '$zipPath' -Force"
                Seq("powershell", "-NoProfile", "-Command", script).!
                ()
              }).start()
            })(using elementBuilder, buildContext)
          )
        )
        ctxItem("Extract here", "mdi2f-folder-open")(_ => {
          new Thread(() => extractFiles(files)).start()
        })
        ctxItem("Create symlink", "mdi2l-link")(_ => {
          new Thread(() => createSymlink(files)).start()
        })
        separatorMenuItem() {}
        ctxItem("Open Terminal Here", "mdi2c-console")(_ => {
          new Thread(() => winConsole(Paths.get(files.head).getParent.toAbsolutePath.toString)).start()
        })
        ctxItem("Properties", "mdi2c-cog")(_ => {
          showPropertiesDialog(files)
        })
      }
    }

    Platform.runLater { () =>
      val node = event.getSource.asInstanceOf[Node]
      val scene = node.getScene

      contextMenuOpen = true

      val closer = new javafx.event.EventHandler[javafx.scene.input.MouseEvent] {
        override def handle(ev: javafx.scene.input.MouseEvent): Unit = {
          if ev.isPrimaryButtonDown then menu.hide()
        }
      }

      scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, closer)

      menu.setOnHidden(_ => {
        scene.removeEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, closer)
        contextMenuOpen = false
      })

      menu.show(node, event.getScreenX, event.getScreenY)
    }
  }

  def winExecuteFile(file: String): Unit = {
    new Thread(() => {
      Seq("cmd", "/c", "start", "", file).!
      ()
    }).start()
  }

  def winConsole(workingDir: String): Unit = {
    new Thread(() => {
      Seq("cmd", "/c", "start", "cmd", "/K", s"cd /d \"$workingDir\"").!
      ()
    }).start()
  }

  def duplicateFiles(files: Seq[String]): Unit = {
    files.foreach { p =>
      val path = Path.of(p)
      val parent = path.getParent
      val name = path.getFileName.toString
      val copyName = s"${name}_copy"
      val target = parent.resolve(copyName)
      if Files.isDirectory(path) then
        Seq("cmd", "/c", "xcopy", "/E", "/I", "/Y", p, target.toString).!
      else
        Files.copy(path, target)
    }
    ()
  }

  def showPropertiesDialog(files: Seq[String]): Unit = {
    val dialog = component[Window[Unit]] {
      WindowsPropertiesDialog(files) {}
    }
    dialog.showAndWaitResult()
  }

  def extractFiles(files: Seq[String]): Unit = {
    files.foreach { f =>
      if f.toLowerCase.endsWith(".zip") then
        val file = new File(f)
        val dir = file.getParentFile.getAbsolutePath
        val script = s"Expand-Archive -LiteralPath '${file.getAbsolutePath}' -DestinationPath '$dir' -Force"
        Seq("powershell", "-NoProfile", "-Command", script).!
    }
    ()
  }

  def createSymlink(files: Seq[String]): Unit = {
    files.foreach { f =>
      val path = Path.of(f)
      val link = path.getParent.resolve(path.getFileName.toString + ".lnk")
      Seq("cmd", "/c", "mklink", link.toString, f).!
    }
    ()
  }
}

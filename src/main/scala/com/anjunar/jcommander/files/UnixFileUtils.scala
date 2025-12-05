package com.anjunar.jcommander.files

import com.anjunar.javafx.dsl.{BuildContext, ElementBuilder, Ref}
import com.anjunar.javafx.dsl.DSL.component
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasSpacing.{alignment, spacing, spacing_=}
import com.anjunar.javafx.dsl.traits.HasStyle.style
import com.anjunar.javafx.dsl.traits.HasText.{text, textProperty}
import com.anjunar.javafx.dsl.traits.HasGraphic.graphic
import com.anjunar.javafx.dsl.ChildBuilder.register
import com.anjunar.javafx.dsl.traits.HasPadding.padding
import com.anjunar.javafx.dsl.traits.HasWidth.prefWidth
import com.anjunar.javafx.dsl.traits.IsNode.vgrow
import com.anjunar.javafx.scene.control.checkbox.{selected, selectedProperty}
import com.anjunar.javafx.scene.control.progressBar.progressProperty
import com.anjunar.javafx.scene.control.{button, checkbox, contextMenu, label, menu, menuItem, progressBar, separatorMenuItem}
import com.anjunar.javafx.scene.layout.{hbox, region, vbox}
import com.anjunar.javafx.scene.window.{close, closeWithResult}
import com.anjunar.javafx.scene.{header, window}
import com.anjunar.javafx.stage.Window
import com.anjunar.jcommander.commands.{DeleteCommand, RenameCommand}
import com.anjunar.jcommander.dsl.Icon.{iconLiteral, iconSize}
import com.anjunar.jcommander.dsl.{ConfirmDialog, FileTable, Icon, ProgressDialog, PropertiesDialog}
import com.anjunar.jcommander.utils.CdiUtils.inject
import com.anjunar.jcommander.{Icons, LinuxNativeCopy}
import javafx.application.Platform
import javafx.beans.property.{SimpleBooleanProperty, SimpleStringProperty}
import javafx.concurrent
import javafx.event.{ActionEvent, EventHandler}
import javafx.geometry.{Insets, Pos}
import javafx.scene.Node
import javafx.scene.control.{ContextMenu, Menu, MenuItem}
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{HBox, Priority}

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File}
import java.nio.file.attribute.{PosixFileAttributes, PosixFilePermissions}
import java.nio.file.{Files, Path, Paths}
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import javax.imageio.ImageIO
import scala.jdk.CollectionConverters.*
import scala.sys.process.*

trait UnixFileUtils extends FileUtils {

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

  override def fileContext(files: Seq[String], event: MouseEvent): Unit = {
    if (files.isEmpty) return
    if (contextMenuOpen) return

    val single = files.size == 1
    val fHead = files.head
    val parentDir = Path.of(fHead).getParent.toString


    val openWithApps = Seq("subl", "code", "gedit", "nano", "vim", "xdg-open")

    val menu = component[ContextMenu] {
      contextMenu() {
        if single then ctxItem("Execute", "mdi2p-play")(_ => {
          executeFile(fHead)
        })
        ctxSubMenu(
          "Open With…",
          "mdi2o-open-in-new",
          openWithApps.map(app =>
            (elementBuilder, buildContext) => ctxItem(app, "mdi2a-application")(_ => {
              new Thread(() => {
                Seq(app, fHead).!
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
            (elementBuilder, buildContext) => ctxItem("Create tar.gz", "mdi2f-folder-download")(_ => {
              new Thread(() => {
                val dir = new File(files.head).getParent
                val names = files.map(f => new File(f).getName)
                val cmd = Seq("tar", "-czf", s"$dir/archive.tar.gz", "-C", dir) ++ names
                cmd.!
                ()
              }).start()
            })(using elementBuilder, buildContext),
            (elementBuilder, buildContext) => ctxItem("Create zip", "mdi2z-zip-box")(_ => {
              new Thread(() => {
                val dir = new File(files.head).getParent
                val names = files.map(f => new File(f).getName)
                val cmd = Seq("zip", "-j", s"$dir/archive.zip") ++ names.map(n => s"$dir/$n")
                cmd.!
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
          new Thread(() => console(Paths.get(files.head).getParent.toAbsolutePath.toString)).start()
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
          if (ev.isPrimaryButtonDown) menu.hide()
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

  def duplicateFiles(files: Seq[String]): Unit =
    new Thread(() => {
      files.foreach { p =>
        val path = Path.of(p)
        val target = path.getParent.resolve(path.getFileName.toString + "_copy")
        Seq("cp", "-r", p, target.toString).!
      }
      ()
    }).start()

  def showPropertiesDialog(files: Seq[String]): Unit = {
    val dialog = component[Window[Unit]] {
      PropertiesDialog(files) {

      }
    }

    dialog.showAndWaitResult()
  }

  def extractFiles(files: Seq[String]): Unit =
    new Thread(() => {
      files.foreach { f =>
        if (f.endsWith(".zip")) Seq("unzip", f).!
        if (f.endsWith(".tar.gz")) Seq("tar", "-xzf", f).!
      }
      ()
    }).start()

  def createSymlink(files: Seq[String]): Unit =
    new Thread(() => {
      files.foreach { f =>
        val path = Path.of(f)
        val link = path.getParent.resolve(path.getFileName.toString + ".link")
        Seq("ln", "-s", f, link.toString).!
      }
      ()
    }).start()

}

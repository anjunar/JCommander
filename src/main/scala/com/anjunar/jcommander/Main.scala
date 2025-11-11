package com.anjunar.jcommander

import scalafx.application.JFXApp3
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.*
import scalafx.scene.layout.*

import java.io.File
import java.nio.file.{FileStore, FileSystems}
import javafx.scene.input.KeyCode
import scalafx.Includes.{jfxMouseEvent2sfx, jfxScene2sfx}

object Main extends JFXApp3 {

  override def start(): Unit = {

    val darkMode = BooleanProperty(true)

    def makeFileTable(): FileTable = FileTable()

    def chooseDirectory(table: FileTable, store: FileStore): Unit = {
      val roots = FileSystems.getDefault.getRootDirectories.iterator()
      while (roots.hasNext) {
        val root = roots.next()
        try {
          val rootStore = java.nio.file.Files.getFileStore(root)
          if (rootStore == store) {
            table.loadDirectory(root.toFile)
            return
          }
        } catch {
          case _: Exception =>
        }
      }
    }

    val leftTable = makeFileTable()
    val rightTable = makeFileTable()

    val leftPane = new FilePane(leftTable, store => chooseDirectory(leftTable, store))
    val rightPane = new FilePane(rightTable, store => chooseDirectory(rightTable, store))

    val toggleTheme = new Button("🌓") {
      tooltip = new Tooltip("Theme wechseln")
      onAction = _ => darkMode.value = !darkMode.value
    }

    val topBar = new HBox {
      spacing = 10
      padding = Insets(6)
      children = Seq(new Label("JCommander File Manager"))
    }

    val splitPane = new SplitPane {
      items.addAll(leftPane, rightPane)
      setDividerPosition(0, 0.5)
    }

    val activeTable = ObjectProperty[FileTable](leftTable)
    val otherTable = ObjectProperty[FileTable](rightTable)
    leftTable.requestFocus()

    val rootPane = new BorderPane {
      top = topBar
      center = splitPane
      bottom = new ActionButtons(toggleTheme, {
        case "F2" => FileUtils.renameFile(activeTable, darkMode)
        case "F5" => FileUtils.copyFiles(activeTable, otherTable, darkMode)
        case "F6" => FileUtils.moveFiles(activeTable, otherTable, darkMode)
        case "F8" => FileUtils.deleteFiles(activeTable, otherTable, darkMode)
      })
    }

    stage = new JFXApp3.PrimaryStage {
      title = "JCommander File Manager"
      width = 1100
      height = 600
      scene = new Scene(rootPane)
    }

    val lightCSS = getClass.getResource("/light-theme.css").toExternalForm
    val darkCSS = getClass.getResource("/dark-theme.css").toExternalForm
    stage.scene().stylesheets.add(darkCSS)

    darkMode.onChange { (_, _, isDark) =>
      val styles = stage.scene().stylesheets
      styles.clear()
      styles.add(if (isDark) darkCSS else lightCSS)
    }

    val home = new File(System.getProperty("user.home"))
    leftTable.loadDirectory(home)
    rightTable.loadDirectory(home)

    def switchFocus(): Unit = {
      if (activeTable.value eq leftTable) {
        activeTable.value = rightTable
        otherTable.value = leftTable
        rightTable.requestFocus()
      } else {
        activeTable.value = leftTable
        otherTable.value = rightTable
        leftTable.requestFocus()
      }
    }

    def openSelectedDirectory(): Unit = {
      val selected = activeTable.value.selectionModel().getSelectedItem
      if (selected != null && selected.file.isDirectory)
        activeTable.value.loadDirectory(selected.file)
    }

    Seq(leftTable, rightTable).foreach { table =>
      table.onMouseClicked = e => {
        if (e.clickCount == 2) {
          val selected = table.selectionModel().getSelectedItem
          if (selected != null && selected.file.isDirectory)
            table.loadDirectory(selected.file)
        }

        activeTable.value = table
        if (table == leftTable) {
          otherTable.value = rightTable
        } else {
          otherTable.value = leftTable
        }
      }

      table.onKeyPressed = e => {
        e.getCode match {
          case KeyCode.TAB =>
            switchFocus()
            e.consume()
          case KeyCode.ENTER =>
            openSelectedDirectory()
            e.consume()
          case KeyCode.F2 => FileUtils.renameFile(activeTable, darkMode)
          case KeyCode.F5 => FileUtils.copyFiles(activeTable, otherTable, darkMode)
          case KeyCode.F6 => FileUtils.moveFiles(activeTable, otherTable, darkMode)
          case KeyCode.F8 => FileUtils.deleteFiles(activeTable, otherTable, darkMode)
          case _ =>
        }
      }
    }
  }
}

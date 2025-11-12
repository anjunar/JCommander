package com.anjunar.jcommander

import com.anjunar.jcommander.components.{ActionButtons, ActiveTable, DarkMode, FilePane, FileTable, HeaderMenuBar}
import com.anjunar.jcommander.files.{FallBackFileUtils, FileUtils, WinFileUtils}
import jakarta.enterprise.inject.se.SeContainerInitializer
import jakarta.enterprise.inject.spi.CDI
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

    val container = SeContainerInitializer.newInstance().initialize()

    val fileUtils = inject(classOf[FileUtils])
    
    val darkMode = inject(classOf[DarkMode])
    
    val leftTable = inject(classOf[FileTable.Left])
    val rightTable = inject(classOf[FileTable.Right])

    val activeTable = inject(classOf[ActiveTable])

    val leftPane = inject(classOf[FilePane.Left])
    val rightPane = inject(classOf[FilePane.Right])

    val topBar = new HBox {
      spacing = 10
      children = new HeaderMenuBar().node
    }

    val splitPane = new SplitPane {
      items.addAll(leftPane.node, rightPane.node)
      setDividerPosition(0, 0.5)
    }

    leftTable.node.requestFocus()

    val actionButtons = inject(classOf[ActionButtons])
    
    val rootPane = new BorderPane {
      top = topBar
      center = splitPane
      bottom = actionButtons.node
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

    darkMode.valueProperty.onChange { (_, _, isDark) =>
      val styles = stage.scene().stylesheets
      styles.clear()
      styles.add(if (isDark) darkCSS else lightCSS)
    }

    val home = new File(System.getProperty("user.home"))
    leftTable.loadDirectory(home)
    rightTable.loadDirectory(home)

    def switchFocus(): Unit = {
      activeTable.swap()
    }

    def onFileEnter(): Unit = {
      val selected = activeTable.active.node.selectionModel().getSelectedItem
      if (selected != null) {
        if (selected.file.isDirectory) {
          activeTable.active.loadDirectory(selected.file)  
        } else {
          fileUtils.executeFile(selected.file) 
        }
      }
    }

    Seq(leftTable, rightTable).foreach { table =>
      table.node.onMouseClicked = e => {
        if (e.clickCount == 2) {
          onFileEnter()
          e.consume()
        }
        activeTable.setActive(table)
      }

      table.node.onKeyPressed = e => {
        e.getCode match {
          case KeyCode.TAB =>
            switchFocus()
            e.consume()
          case KeyCode.ENTER =>
            onFileEnter()
            e.consume()
          case KeyCode.F2 => fileUtils.renameFile(activeTable.active)
          case KeyCode.F5 => fileUtils.copyFiles(activeTable.active, activeTable.inActive)
          case KeyCode.F6 => fileUtils.moveFiles(activeTable.active, activeTable.inActive)
          case KeyCode.F7 => fileUtils.mkDir(activeTable.active)
          case KeyCode.F8 => fileUtils.deleteFiles(activeTable.active, activeTable.inActive)
          case _ =>
        }
      }
    }
  }
  
}

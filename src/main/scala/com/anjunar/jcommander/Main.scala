package com.anjunar.jcommander

import com.anjunar.jcommander.commands.*
import com.anjunar.jcommander.components.*
import com.anjunar.jcommander.configuration.Configuration
import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.objectmapper.ObjectMapperBuilder
import com.anjunar.scala.universe.introspector.BeanIntrospector
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.enterprise.inject.se.SeContainerInitializer
import jakarta.inject.Inject
import javafx.scene.input.KeyCode
import org.kordamp.ikonli.javafx.FontIcon
import scalafx.Includes.{jfxMouseEvent2sfx, jfxScene2sfx}
import scalafx.application.JFXApp3
import scalafx.scene.control.{ContextMenu, MenuItem}
import scalafx.scene.input.MouseButton
import scalafx.Includes.*
import scalafx.scene.paint.Color

import java.io.File

object Main extends JFXApp3 {

  override def start(): Unit = {

    val container = SeContainerInitializer.newInstance().initialize()

    val configuration = inject(classOf[Configuration])

    val objectMapper = ObjectMapperBuilder.build()

    val homeDir = System.getProperty("user.home")
    val configDir = new File(homeDir, ".jcommander")
    val configFile = new File(configDir, "configuration.json")

    def loadConfiguration(target: AnyRef, source: AnyRef, clazz: Class[? <: AnyRef]): Unit = {
      val beanModel = BeanIntrospector.createWithType(clazz)
      beanModel.properties.foreach(property => {
        if (property.findAnnotation(classOf[JsonProperty]) != null) {

          val sourceValue = property.get(source)
          val targetValue = property.get(target)

          if (property.findAnnotation(classOf[Inject]) != null) {
            loadConfiguration(targetValue.asInstanceOf[AnyRef], sourceValue.asInstanceOf[AnyRef], property.propertyType.raw.asInstanceOf[Class[AnyRef]])
          } else {
            property.set(target, sourceValue)
          }
        }
      })
    }

    if (configFile.exists()) {
      val loadedConf = objectMapper.readValue(configFile, classOf[Configuration])

      loadConfiguration(configuration, loadedConf, classOf[Configuration])
    }

    val fileUtils = inject(classOf[FileUtils])

    val darkMode = inject(classOf[DarkMode])

    val leftTable = inject(classOf[FileTable.Left])
    val rightTable = inject(classOf[FileTable.Right])

    val activeTable = inject(classOf[ActiveTable])

    leftTable.node.requestFocus()

    val actionButtons = inject(classOf[ActionButtons])

    stage = inject(classOf[PrimaryStage]).node

    val lightCSS = getClass.getResource("/light-theme.css").toExternalForm
    val darkCSS = getClass.getResource("/dark-theme.css").toExternalForm
    stage.scene().stylesheets.add(if darkMode.value then darkCSS else lightCSS)

    darkMode.valueProperty.onChange { (_, _, isDark) =>
      val styles = stage.scene().stylesheets
      styles.clear()
      styles.add(if (isDark) darkCSS else lightCSS)
    }

    leftTable.loadDirectory(configuration.primaryStage.leftTable.file)
    rightTable.loadDirectory(configuration.primaryStage.rightTable.file)

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

    def themedIcon(iconName: String, size: Int = 20): FontIcon = {
      val icon = new FontIcon(iconName)
      icon.setIconSize(size)
      icon.setIconColor(if (darkMode.value) Color.White else Color.Black)

      // reaktiv anpassen, wenn das Theme wechselt
      darkMode.valueProperty.onChange { (_, _, isDark) =>
        icon.setIconColor(if (isDark) Color.White else Color.Black)
      }

      icon
    }

    Seq(leftTable, rightTable).foreach { table =>

      val contextMenu = new ContextMenu(
        new MenuItem("Rename") {
          graphic = themedIcon("mdi2t-text-box-edit-outline")
          onAction = _ => inject(classOf[RenameCommand]).execute()
        },
        new MenuItem("Copy") {
          graphic = themedIcon("mdi2c-content-copy")
          onAction = _ => inject(classOf[CopyCommand]).execute()
        },
        new MenuItem("Move") {
          graphic = themedIcon("mdi2f-file-move-outline")
          onAction = _ => inject(classOf[MoveCommand]).execute()
        },
        new MenuItem("Delete") {
          graphic = themedIcon("mdi2d-delete-outline")
          onAction = _ => inject(classOf[DeleteCommand]).execute()
        },
        new MenuItem("Make Directory") {
          graphic = themedIcon("mdi2f-folder-plus-outline")
          onAction = _ => inject(classOf[MkDirCommand]).execute()
        }
      ) {
        style = "-fx-padding: 8 12 8 12;"
      }
      
      contextMenu.setAutoHide(true)

      table.node.onMouseClicked = e => {
        activeTable.setActive(table)
        contextMenu.hide()

        if (e.button == MouseButton.Secondary) {
          contextMenu.show(table.node, e.screenX, e.screenY)
          e.consume()
        } else if (e.clickCount == 2) {
          onFileEnter()
          e.consume()
        }
      }

      table.node.onKeyPressed = e => {
        e.getCode match {
          case KeyCode.TAB =>
            switchFocus()
            e.consume()
          case KeyCode.ENTER =>
            onFileEnter()
            e.consume()
          case KeyCode.F2 => inject(classOf[RenameCommand]).execute()
          case KeyCode.F3 => inject(classOf[EditCommand]).execute()
          case KeyCode.F4 => inject(classOf[ConsoleCommand]).execute()
          case KeyCode.F5 => inject(classOf[CopyCommand]).execute()
          case KeyCode.F6 => inject(classOf[MoveCommand]).execute()
          case KeyCode.F7 => inject(classOf[MkDirCommand]).execute()
          case KeyCode.F8 => inject(classOf[DeleteCommand]).execute()
          case KeyCode.F10 => inject(classOf[QuitCommand]).execute()
          case _ =>
        }
      }

    }

  }
  
}

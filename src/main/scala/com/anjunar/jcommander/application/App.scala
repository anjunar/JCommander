package com.anjunar.jcommander.application

import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.traits.HasHeaderButtons.{closeable, maximizable, minimizable}
import com.anjunar.javafx.dsl.traits.HasPadding.padding
import com.anjunar.javafx.dsl.traits.HasSpacing.alignment
import com.anjunar.javafx.dsl.traits.HasText.text
import com.anjunar.javafx.dsl.traits.IsNode.vgrow
import com.anjunar.javafx.scene.control.splitPane.dividerPositions
import com.anjunar.javafx.scene.control.{label, splitPane}
import com.anjunar.javafx.scene.layout.{hbox, vbox}
import com.anjunar.javafx.scene.window.resizable
import com.anjunar.javafx.scene.{header, window}
import com.anjunar.javafx.stage.Resizable
import com.anjunar.jcommander.configuration.{Configuration, DarkModeConf}
import com.anjunar.jcommander.dsl.FilePane.onTableChange
import com.anjunar.jcommander.dsl.{ActionButtons, FilePane, MainMenu}
import com.anjunar.jcommander.manager.FileTableManager
import com.anjunar.jcommander.objectmapper.ObjectMapperBuilder
import com.anjunar.jcommander.utils.CdiUtils.inject
import com.anjunar.jcommander.utils.{NativeUtils, OSType}
import com.anjunar.scala.universe.introspector.BeanIntrospector
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.enterprise.inject.se.SeContainerInitializer
import jakarta.inject.Inject
import javafx.application.Application
import javafx.geometry.{Insets, Pos}
import javafx.scene.Scene
import javafx.scene.layout.{Priority, VBox}
import javafx.stage.{Stage, StageStyle}

import java.io.File

class App extends Application {

  override def start(primaryStage: Stage): Unit = {
    
    try {
      OSType.osName match {
        case "win" => NativeUtils.loadWinNativeCopy("win_native_copy.dll")
        case "linux" => NativeUtils.loadWinNativeCopy("linux_native_copy.so")
        case "mac" => NativeUtils.loadWinNativeCopy("osx_native_copy.dylib")
      }
    } catch {
      case e : Exception => OSType.fallback = true 
    }

    val container = SeContainerInitializer.newInstance().initialize()

    val configuration = inject(classOf[Configuration])

    val fileTableManager = inject(classOf[FileTableManager])

    val objectMapper = ObjectMapperBuilder.build()

    val configDir = ConfigDir.path()
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

    val darkMode = inject(classOf[DarkModeConf])

    val ui = component[Stage] {
      window(configuration.primaryStage.width, configuration.primaryStage.height, primaryStage) {
        minimizable = true
        maximizable = true
        closeable = true
        resizable = true

        header() {
          hbox() {
            padding = new Insets(0, 0, 0, 10)
            alignment = Pos.CENTER_LEFT
            label() {
              text = "JCommander"
            }
            MainMenu() {}
          }
        }
        vbox() {
          vgrow = Priority.ALWAYS
          splitPane() {
            dividerPositions = Array(0.5)
            vgrow = Priority.ALWAYS
            vbox() {
              FilePane() {
                vgrow = Priority.ALWAYS
                onTableChange = table => {
                  fileTableManager.loadLeft(table)
                }
              }
            }
            vbox() {
              FilePane() {
                vgrow = Priority.ALWAYS
                onTableChange = table => {
                  fileTableManager.loadRight(table)
                }
              }
            }
          }
          ActionButtons() {}
        }
      }
    }

    ui.show()


  }

}

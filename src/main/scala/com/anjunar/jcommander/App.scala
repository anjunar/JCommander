package com.anjunar.jcommander

import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.jcommander.configuration.{Configuration, DarkModeConf}
import com.anjunar.jcommander.dsl.FilePane.HastLocalFileTable.*
import com.anjunar.jcommander.dsl.{ActionButtons, FilePane, MainMenu}
import com.anjunar.jcommander.manager.FileTableManager
import com.anjunar.jcommander.objectmapper.ObjectMapperBuilder
import com.anjunar.jcommander.ui.Resizable
import com.anjunar.jcommander.utils.CdiUtils.inject
import com.anjunar.scala.universe.introspector.BeanIntrospector
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.enterprise.inject.se.SeContainerInitializer
import jakarta.inject.Inject
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.{Priority, VBox}
import javafx.stage.{Stage, StageStyle}

import java.io.File

class App extends Application {

  override def start(primaryStage: Stage): Unit = {

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
          label() {
            text = "JCommander"
          }
          MainMenu() {}
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
                  fileTableManager.loadRight(table, true)
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

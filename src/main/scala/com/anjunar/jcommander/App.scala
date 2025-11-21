package com.anjunar.jcommander

import javafx.application.Application
import javafx.scene.{Group, Scene}
import javafx.stage.{Stage, StageStyle}
import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.traits.HasNode
import com.anjunar.jcommander.configuration.{Configuration, DarkModeConf}
import com.anjunar.jcommander.dsl.{ActionButtons, FilePane, MainTitleBar}
import com.anjunar.jcommander.dsl.MainTitleBar.*
import com.anjunar.jcommander.files.FileItem
import com.anjunar.jcommander.objectmapper.ObjectMapperBuilder
import com.anjunar.jcommander.ui.Resizable
import com.anjunar.jcommander.utils.CdiUtils.inject
import com.anjunar.scala.universe.introspector.BeanIntrospector
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.enterprise.inject.se.SeContainerInitializer
import jakarta.inject.Inject
import javafx.scene.layout.{HBox, Priority, VBox}

import java.io.File

class App extends Application {

  override def start(primaryStage: Stage): Unit = {

    val container = SeContainerInitializer.newInstance().initialize()

    val configuration = inject(classOf[Configuration])

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

    val ui = component[VBox] {
      vbox() {
        style = "-fx-border-color: #444; -fx-border-width: 1;"
        MainTitleBar(primaryStage) {}
        splitPane() {
          dividerPositions = Array(0.5)
          vgrow = Priority.ALWAYS
          vbox() {
            FilePane() {
              vgrow = Priority.ALWAYS
            }
          }
          vbox() {
            FilePane() {
              vgrow = Priority.ALWAYS
            }
          }
        }
        ActionButtons() {}
      }
    }

    val scene = new Scene(ui, 1000, 600)
    new Resizable(primaryStage, ui)

    val lightCSS = getClass.getResource("/light-theme.css").toExternalForm
    val darkCSS = getClass.getResource("/dark-theme.css").toExternalForm
    scene.getStylesheets.add(if darkMode.value then darkCSS else lightCSS)

    primaryStage.setScene(scene)
    primaryStage.initStyle(StageStyle.UNDECORATED)
    primaryStage.show()


  }

}

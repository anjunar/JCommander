package com.anjunar.jcommander

import com.anjunar.jcommander.commands.*
import com.anjunar.jcommander.components.*
import com.anjunar.jcommander.utils.CdiUtils.*
import com.anjunar.jcommander.configuration.{Configuration, DarkModeConf}
import com.anjunar.jcommander.files.FileUtils
import com.anjunar.jcommander.manager.FileTableManager
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

    val darkMode = inject(classOf[DarkModeConf])

    val fileTableManager = inject(classOf[FileTableManager])

    val actionButtons = new ActionButtonsComponent()

    stage = new PrimaryStageComponent().node

    val lightCSS = getClass.getResource("/light-theme.css").toExternalForm
    val darkCSS = getClass.getResource("/dark-theme.css").toExternalForm
    stage.scene().stylesheets.add(if darkMode.value then darkCSS else lightCSS)

    darkMode.valueProperty.onChange { (_, _, isDark) =>
      val styles = stage.scene().stylesheets
      styles.clear()
      styles.add(if (isDark) darkCSS else lightCSS)
    }

  }
  
}

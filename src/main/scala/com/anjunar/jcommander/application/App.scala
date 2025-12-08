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
import com.anjunar.jcommander.utils.{NativeUtils, OSType}
import com.fasterxml.jackson.annotation.JsonProperty
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
        case "win" => NativeUtils.load("win_native_copy.dll")
        case "linux" => NativeUtils.load("linux_native_copy.so")
        case "mac" => NativeUtils.load("osx_native_copy.dylib")
      }
    } catch {
      case e : Exception => OSType.fallback = true 
    }

    val fileTableManager = FileTableManager()

    val darkMode = DarkModeConf()
    
    val configuration = ConfigurationLoader.load()

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

    ui.setX(configuration.primaryStage.x)
    ui.setY(configuration.primaryStage.y)

    primaryStage.widthProperty().addListener((_, _, newValue) => {
      configuration.primaryStage.width = newValue.doubleValue()
    })

    primaryStage.heightProperty().addListener((_, _, newValue) => {
      configuration.primaryStage.height = newValue.doubleValue()
    })

    primaryStage.xProperty().addListener((_, _, newValue) => {
      configuration.primaryStage.x = newValue.doubleValue()
    })

    primaryStage.yProperty().addListener((_, _, newValue) => {
      configuration.primaryStage.y = newValue.doubleValue()
    })

    ui.show()


  }

}

package com.anjunar.jcommander.dsl

import com.anjunar.javafx.dsl.*
import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.traits.HasOnAction.{onAction, onAction_=}
import com.anjunar.javafx.dsl.traits.HasPadding.padding
import com.anjunar.javafx.dsl.traits.HasSpacing.{alignment, spacing}
import com.anjunar.javafx.dsl.traits.HasText.{text, textProperty}
import com.anjunar.javafx.dsl.traits.IstTextInput.promptText
import com.anjunar.javafx.scene.control.{button, label, menu, menuBar, menuItem, textField}
import com.anjunar.javafx.scene.layout.{hbox, vbox}
import com.anjunar.javafx.scene.window.{close, closeWithResult}
import com.anjunar.javafx.scene.{header, window}
import com.anjunar.javafx.stage.Window
import com.anjunar.jcommander.commands.QuitCommand
import com.anjunar.jcommander.dsl.dialog.AboutDialog
import com.anjunar.jcommander.dsl.window.ConfigurationWindow
import com.anjunar.jcommander.files.FileUtilsProducer
import com.anjunar.jcommander.manager.FileTableManager
import com.sun.javafx.css.SimpleSelector
import javafx.beans.property.{SimpleStringProperty, StringProperty}
import javafx.geometry.{Insets, Pos}
import javafx.scene.layout.HBox

import java.nio.file.{Files, Path}

class MainMenu extends NodeBuilder[HBox] {

  private val fileUtils = FileUtilsProducer.produce()

  private val tableManager = FileTableManager()

  lazy val node: HBox = {
    val mainMenu = component[HBox] {
      hbox() {
        padding = new Insets(2, 0, 0, -16)
        menuBar() {
          menu() {
            text = "JCommander"
            menuItem() {
              text = "Configuration"
              onAction = event => {
                val dialog = component[Window[Unit]] {
                  ConfigurationWindow() {}
                }

                dialog.show()
              }
            }
            menuItem() {
              text = "About"
              onAction = event => {
                val dialog = component[Window[Unit]] {
                  AboutDialog() {}
                }

                dialog.show()
              }
            }
            menuItem() {
              text = "Exit"
              onAction = event => {
                new QuitCommand().execute()
              }
            }
          }
          menu() {
            text = "File"
            menuItem() {
              text = "New"

              onAction = _ => {

                val directoryProperty = new SimpleStringProperty("")

                val dialog = component[Window[String]] {
                  window[String]() {
                    header() {
                      label() {
                        text = "Create File"
                      }
                    }

                    vbox() {
                      spacing = 10
                      padding = new Insets(10)

                      textField() {
                        promptText = "File Name"
                        textProperty((prop: StringProperty) => prop.bindBidirectional(directoryProperty))
                      }

                      hbox() {
                        spacing = 10
                        alignment = Pos.CENTER_RIGHT
                        button() {
                          text = "Ok"
                          onAction = _ => {
                            closeWithResult("Ok")
                          }
                        }

                        button() {
                          text = "Cancel"
                          onAction = _ => {
                            close()
                          }
                        }
                      }
                    }
                  }
                }

                dialog.showAndWaitResult().foreach(result => {
                  if (result == "Ok") {
                    val directory = tableManager.source.directoryProperty.get()
                    Files.createFile(Path.of(directory).resolve(directoryProperty.get()))
                  }
                })
              }
            }
            menuItem() {
              text = "Open"

              onAction = _ => {
                val item = tableManager.source.node.getSelectionModel.getSelectedItem
                if (item != null) {
                  fileUtils.executeFile(item.file)
                }
              }
            }
          }
        }
      }
    }

    mainMenu
  }

  override def build(): HBox = node
}

object MainMenu extends Producer[MainMenu, HBox] {

  override def createBuilder: MainMenu = new MainMenu()

}

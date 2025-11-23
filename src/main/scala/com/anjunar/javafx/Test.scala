package com.anjunar.javafx

import com.anjunar.javafx.dsl.DSL.*
import com.anjunar.javafx.dsl.Ref
import com.anjunar.javafx.dsl.traits.HasOnAction.onAction
import com.anjunar.javafx.dsl.traits.HasText.text
import com.anjunar.javafx.dsl.ChildNodeBuilder.register
import com.anjunar.javafx.scene.control.{button, label}
import com.anjunar.javafx.scene.layout.vbox
import com.anjunar.jcommander.files.FileItem
import javafx.application.Application
import javafx.scene.control.TableView
import javafx.scene.layout.VBox
import javafx.scene.{Group, Scene}
import javafx.stage.Stage

class Test extends Application:

  val labelRef = Ref[label]()

  override def start(stage: Stage): Unit =
    val rootNode =
      component[VBox] {
        vbox() {
          label(labelRef) {
            text = "Hello World"
          }

          val label2 = label.build() {
            text = "Hello World 2"
          }

          register(label2)

          button() {
            text = "Click Me"
            onAction = event =>
              println("Button Clicked")
              labelRef {
                text = "Running..."
              }
          }
        }
      }

    val box = new Group(rootNode)
    
    val scene = new Scene(box, 800, 600)
    stage.setScene(scene)
    stage.setTitle("JCommander (JavaFX DSL)")
    stage.show()

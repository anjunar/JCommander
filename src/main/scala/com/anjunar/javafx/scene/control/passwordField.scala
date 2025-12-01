package com.anjunar.javafx.scene.control

import com.anjunar.javafx.dsl.traits.{HasText, IstTextInput}
import com.anjunar.javafx.dsl.{NodeBuilder, Producer}
import javafx.scene.control.PasswordField

class passwordField extends NodeBuilder[PasswordField], HasText, IstTextInput {
  override lazy val node: PasswordField = new PasswordField()

  override def build(): PasswordField = node
}

object passwordField extends Producer[passwordField, PasswordField] {
  override def createBuilder: passwordField = new passwordField()
}

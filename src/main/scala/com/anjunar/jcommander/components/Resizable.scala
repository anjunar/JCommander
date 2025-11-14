package com.anjunar.jcommander.components

import scalafx.Includes.*
import scalafx.scene.Node
import scalafx.stage.Stage
import scalafx.scene.input.MouseEvent

class Resizable(stage: Stage, root: Node) {

  private val border = 6
  private var x = 0.0
  private var y = 0.0

  root.addEventFilter(MouseEvent.MouseMoved, (e: MouseEvent) => {
    val w = stage.getWidth
    val h = stage.getHeight
    val xPos = e.getX
    val yPos = e.getY

    val left = xPos < border
    val right = xPos > w - border
    val top = yPos < border
    val bottom = yPos > h - border

    if (left && top) root.setCursor(javafx.scene.Cursor.NW_RESIZE)
    else if (left && bottom) root.setCursor(javafx.scene.Cursor.SW_RESIZE)
    else if (right && top) root.setCursor(javafx.scene.Cursor.NE_RESIZE)
    else if (right && bottom) root.setCursor(javafx.scene.Cursor.SE_RESIZE)
    else if (left) root.setCursor(javafx.scene.Cursor.W_RESIZE)
    else if (right) root.setCursor(javafx.scene.Cursor.E_RESIZE)
    else if (top) root.setCursor(javafx.scene.Cursor.N_RESIZE)
    else if (bottom) root.setCursor(javafx.scene.Cursor.S_RESIZE)
    else root.setCursor(javafx.scene.Cursor.DEFAULT)
  })

  root.addEventFilter(MouseEvent.MousePressed, (e: MouseEvent) => {
    x = e.getSceneX
    y = e.getSceneY
  })

  root.addEventFilter(MouseEvent.MouseDragged, (e: MouseEvent) => {
    val w = stage.getWidth
    val h = stage.getHeight
    val dx = e.getSceneX
    val dy = e.getSceneY

    root.getCursor match {
      case javafx.scene.Cursor.E_RESIZE =>
        stage.setWidth(dx)
      case javafx.scene.Cursor.S_RESIZE =>
        stage.setHeight(dy)
      case javafx.scene.Cursor.SE_RESIZE =>
        stage.setWidth(dx)
        stage.setHeight(dy)
      case javafx.scene.Cursor.W_RESIZE =>
        val newW = w - (dx - x)
        if (newW > stage.getMinWidth) {
          stage.setX(e.getScreenX - x)
          stage.setWidth(newW)
        }
      case javafx.scene.Cursor.N_RESIZE =>
        val newH = h - (dy - y)
        if (newH > stage.getMinHeight) {
          stage.setY(e.getScreenY - y)
          stage.setHeight(newH)
        }
      case javafx.scene.Cursor.NW_RESIZE =>
        val newW = w - (dx - x)
        val newH = h - (dy - y)
        if (newW > stage.getMinWidth) {
          stage.setX(e.getScreenX - x)
          stage.setWidth(newW)
        }
        if (newH > stage.getMinHeight) {
          stage.setY(e.getScreenY - y)
          stage.setHeight(newH)
        }
      case javafx.scene.Cursor.NE_RESIZE =>
        stage.setWidth(dx)
        val newH = h - (dy - y)
        if (newH > stage.getMinHeight) {
          stage.setY(e.getScreenY - y)
          stage.setHeight(newH)
        }
      case javafx.scene.Cursor.SW_RESIZE =>
        val newW = w - (dx - x)
        if (newW > stage.getMinWidth) {
          stage.setX(e.getScreenX - x)
          stage.setWidth(newW)
        }
        stage.setHeight(dy)
      case _ =>
    }
  })
}

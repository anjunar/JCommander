package com.anjunar.javafx.scene.image

import com.anjunar.javafx.dsl.{NodeBuilder, Producer}
import com.anjunar.jcommander.utils.AutoBindObservableProperties
import javafx.scene.image.{Image as jFxImage, ImageView as jFxImageView}

class imageView extends NodeBuilder[jFxImageView] {

  lazy val node : jFxImageView = new jFxImageView
  
  override def build(): jFxImageView = node
  
}

object imageView extends Producer[imageView, jFxImageView] {
  override def createBuilder: imageView = new imageView

  def fitWidth()(using h: imageView): Double = h.node.getFitWidth

  def fitWidth_=(v: Double)(using h: imageView): Unit = h.node.setFitWidth(v)

  def fitHeight()(using h: imageView): Double = h.node.getFitHeight

  def fitHeight_=(v: Double)(using h: imageView): Unit = h.node.setFitHeight(v)

  def image()(using h: imageView): jFxImage = h.node.getImage

  def image_=(v: jFxImage)(using h: imageView): Unit = h.node.setImage(v)

}

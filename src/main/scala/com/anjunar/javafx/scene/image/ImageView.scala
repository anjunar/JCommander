package com.anjunar.javafx.scene.image

import com.anjunar.javafx.dsl.{NodeBuilder, Producer}
import com.anjunar.javafx.scene.image.ImageView.HasImageView
import javafx.scene.image.ImageView as jFxImageView
import javafx.scene.image.Image as jFxImage

class ImageView extends NodeBuilder[jFxImageView], HasImageView {
  
  override val node: jFxImageView = new jFxImageView
  
  override def build(): jFxImageView = node
  
}

object ImageView extends Producer[ImageView, jFxImageView] {
  override def createBuilder: ImageView = new ImageView
  
  trait HasImageView {
    val node : jFxImageView
  }
  
  object HasImageView {
    def fitWidth()(using h: HasImageView): Double = h.node.getFitWidth
    def fitWidth_=(v: Double)(using h: HasImageView): Unit = h.node.setFitWidth(v)

    def fitHeight()(using h: HasImageView): Double = h.node.getFitHeight
    def fitHeight_=(v: Double)(using h: HasImageView): Unit = h.node.setFitHeight(v)

    def image()(using h: HasImageView): jFxImage = h.node.getImage
    def image_=(v: jFxImage)(using h: HasImageView): Unit = h.node.setImage(v)

  }
  
}

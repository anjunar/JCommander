package com.anjunar.jcommander.utils

import jakarta.enterprise.inject.spi.CDI

import scala.jdk.CollectionConverters.*

object CdiUtils {

  def inject[T](c: Class[T]): T = CDI.current.select(c).get

  def injectInstance[T](c: Class[T]): Seq[T] = CDI.current.select(c).asScala.toSeq

}

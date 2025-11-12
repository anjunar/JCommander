package com.anjunar

import jakarta.enterprise.inject.spi.CDI

package object jcommander {

  def inject[T](c: Class[T]): T = CDI.current.select(c).get

}

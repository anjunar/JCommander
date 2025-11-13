package com.anjunar.jcommander.objectmapper

import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.module.SimpleModule
import org.jboss.weld.proxy.WeldClientProxy

class CdiTypeResolver extends AbstractTypeResolver {
  override def findTypeMapping(config: DeserializationConfig, typeToResolve: JavaType): JavaType = {
    val raw = typeToResolve.getRawClass
    if (classOf[WeldClientProxy].isAssignableFrom(raw)) {
      try {
        config.constructType(raw.getSuperclass)
      } catch {
        case _: Throwable => typeToResolve
      }
    } else typeToResolve
  }
}

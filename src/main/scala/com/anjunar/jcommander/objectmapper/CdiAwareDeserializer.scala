package com.anjunar.jcommander.objectmapper

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer, JsonNode, ObjectMapper}
import jakarta.enterprise.inject.spi.CDI
import org.jboss.weld.exceptions.UnsatisfiedResolutionException
import org.jboss.weld.proxy.WeldClientProxy

class CdiAwareDeserializer[E](clazz : Class[E]) extends JsonDeserializer[E] {

  override def deserialize(p: JsonParser, ctxt: DeserializationContext): E = {
    val mapper = ObjectMapperBuilder.buildWithoutCDI()
    val node: JsonNode = mapper.readTree(p)

    try {
      val cdi = CDI.current()
      val instance = cdi.select(clazz).get()
      val proxy : WeldClientProxy = instance.asInstanceOf[WeldClientProxy]
      mapper.readerForUpdating(proxy.getMetadata.getContextualInstance).readValue(node)
      instance
    } catch {
      case ex : Exception => mapper.treeToValue(node, clazz)
    }
  }
}

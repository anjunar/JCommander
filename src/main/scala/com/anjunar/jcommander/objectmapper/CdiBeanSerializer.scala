package com.anjunar.jcommander.objectmapper

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonNode, JsonSerializer, SerializerProvider}
import org.jboss.weld.proxy.WeldClientProxy

class CdiBeanSerializer extends JsonSerializer[WeldClientProxy] {
  override def serialize(proxy: WeldClientProxy, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    if (proxy == null) {
      gen.writeNull()
    } else {
      val instance = proxy.getMetadata.getContextualInstance
      serializers.defaultSerializeValue(instance, gen)
    }
  }
}
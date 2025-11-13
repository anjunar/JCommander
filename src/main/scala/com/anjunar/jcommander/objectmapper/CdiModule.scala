package com.anjunar.jcommander.objectmapper

import com.anjunar.jcommander.configuration.{Configuration, PrimaryStageConf, SublimeConf}
import com.fasterxml.jackson.databind.module.SimpleModule
import org.jboss.weld.proxy.WeldClientProxy

class CdiModule extends SimpleModule("CdiModule") {
  addSerializer(classOf[WeldClientProxy], new CdiBeanSerializer)
  addDeserializer(classOf[SublimeConf], new CdiAwareDeserializer(classOf[SublimeConf]))
  addDeserializer(classOf[PrimaryStageConf], new CdiAwareDeserializer(classOf[PrimaryStageConf]))
  addDeserializer(classOf[Configuration], new CdiAwareDeserializer(classOf[Configuration]))

}
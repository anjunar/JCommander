package com.anjunar.jcommander.objectmapper

import com.anjunar.jcommander.configuration.{Configuration, DarkModeConf, PrimaryStageConf, TextEditorConf}
import com.fasterxml.jackson.databind.module.SimpleModule
import org.jboss.weld.proxy.WeldClientProxy

class CdiModule extends SimpleModule("CdiModule") {
  addSerializer(classOf[WeldClientProxy], new CdiBeanSerializer)
}
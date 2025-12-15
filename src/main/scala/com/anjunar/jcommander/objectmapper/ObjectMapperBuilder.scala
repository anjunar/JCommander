package com.anjunar.jcommander.objectmapper

import com.anjunar.jcommander.configuration.{PrimaryStageConf, TextEditorConf}
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.databind.{BeanProperty, DeserializationContext, InjectableValues, ObjectMapper}

import java.lang

object ObjectMapperBuilder {

  def build(): ObjectMapper = {
    val objectMapper = new ObjectMapper()

    objectMapper.setVisibility(
      objectMapper.getDeserializationConfig
        .getDefaultVisibilityChecker
        .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
    )

    objectMapper.setVisibility(
      objectMapper.getSerializationConfig
        .getDefaultVisibilityChecker
        .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
    )

    objectMapper
  }

}

package com.anjunar.jcommander.objectmapper

import com.anjunar.jcommander.configuration.{PrimaryStageConf, SublimeConf}
import com.anjunar.jcommander.inject
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.databind.{BeanProperty, DeserializationContext, InjectableValues, ObjectMapper}

import java.lang

object ObjectMapperBuilder {

  def build(): ObjectMapper = {
    val objectMapper = new ObjectMapper()
    objectMapper.registerModule(new CdiModule)

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

    val injectableValues = new InjectableValues.Std() {
      override def findInjectableValue(ctxt: DeserializationContext,
                                       valueId: Any,
                                       forProperty: BeanProperty,
                                       beanInstance: Any,
                                       optional: lang.Boolean,
                                       useInput: lang.Boolean): AnyRef = {

        valueId match {
          case id: String =>
            id match {
              case "primaryStage" => inject(classOf[PrimaryStageConf])
              case "sublime" => inject(classOf[SublimeConf])
              case _ => null
            }

          case clazz: Class[_] =>
            inject(clazz.asInstanceOf[Class[AnyRef]])

          case _ => null
        }
      }
    }

    objectMapper.setInjectableValues(injectableValues)

    objectMapper
  }

  def buildWithoutCDI(): ObjectMapper = {
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

    val injectableValues = new InjectableValues.Std() {
      override def findInjectableValue(ctxt: DeserializationContext,
                                       valueId: Any,
                                       forProperty: BeanProperty,
                                       beanInstance: Any,
                                       optional: lang.Boolean,
                                       useInput: lang.Boolean): AnyRef = {

        valueId match {
          case id: String =>
            id match {
              case "primaryStage" => inject(classOf[PrimaryStageConf])
              case "sublime" => inject(classOf[SublimeConf])
              case _ => null
            }

          case clazz: Class[_] =>
            inject(clazz.asInstanceOf[Class[AnyRef]])

          case _ => null
        }
      }
    }

    objectMapper.setInjectableValues(injectableValues)

    objectMapper
  }


}

package com.anjunar.jcommander.application

import com.anjunar.jcommander.configuration.Configuration
import com.anjunar.jcommander.objectmapper.ObjectMapperBuilder
import com.anjunar.jcommander.utils.CdiUtils.inject
import com.anjunar.scala.universe.introspector.BeanIntrospector
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.inject.Inject

import java.io.File

object ConfigurationLoader {
  
  def load() : Configuration = {
    
    val configuration = inject(classOf[Configuration])

    val objectMapper = ObjectMapperBuilder.build()

    val configDir = ConfigDir.path()
    val configFile = new File(configDir, "configuration.json")

    def loadConfiguration(target: AnyRef, source: AnyRef, clazz: Class[? <: AnyRef]): Unit = {
      val beanModel = BeanIntrospector.createWithType(clazz)
      beanModel.properties.foreach(property => {
        if (property.findAnnotation(classOf[JsonProperty]) != null) {

          val sourceValue = property.get(source)
          val targetValue = property.get(target)

          if (property.findAnnotation(classOf[Inject]) != null) {
            loadConfiguration(targetValue.asInstanceOf[AnyRef], sourceValue.asInstanceOf[AnyRef], property.propertyType.raw.asInstanceOf[Class[AnyRef]])
          } else {
            property.set(target, sourceValue)
          }
        }
      })
    }

    if (configFile.exists()) {
      val loadedConf = objectMapper.readValue(configFile, classOf[Configuration])

      loadConfiguration(configuration, loadedConf, classOf[Configuration])
    }
    
    configuration
  }

}

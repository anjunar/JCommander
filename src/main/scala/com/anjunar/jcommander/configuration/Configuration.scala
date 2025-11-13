package com.anjunar.jcommander.configuration

import com.anjunar.jcommander.inject
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class Configuration {
  
  val sublimeConf: SublimeConf = inject(classOf[SublimeConf])
  
}

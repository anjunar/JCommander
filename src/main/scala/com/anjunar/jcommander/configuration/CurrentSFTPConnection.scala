package com.anjunar.jcommander.configuration

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class CurrentSFTPConnection {

  var host: String = "192.168.172.25"
  
  var port: Int = 22

}

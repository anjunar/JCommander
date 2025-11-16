package com.anjunar.jcommander.configuration

class SFTPConnection {

  var host: String = "127.0.0.1"
  
  var port: Int = 22
  
  var username: String = "root"
  
  var password: String = ""
  
}

object SFTPConnection {
  
  def apply(host : String, port : Int, username : String, password : String): SFTPConnection = {
    val newConnection = new SFTPConnection()
    newConnection.host = host
    newConnection.port = port
    newConnection.username = username
    newConnection.password = password
    newConnection
  }
  
}

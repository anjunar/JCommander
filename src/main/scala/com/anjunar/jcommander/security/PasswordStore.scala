package com.anjunar.jcommander.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import java.nio.file.{Files, Path}
import java.util.Base64

class PasswordStore(path: Path, master: Path) {

  private val mapper = new ObjectMapper().registerModule(DefaultScalaModule)

  private def loadJson(): ObjectNode = {
    if (!Files.exists(path)) {
      Files.createDirectories(path.getParent)
      mapper.createObjectNode()
    } else {
      mapper.readTree(path.toFile).asInstanceOf[ObjectNode]
    }
  }

  private def saveJson(obj: ObjectNode): Unit = {
    mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile, obj)
  }

  def savePassword(key: String, value: String): Unit = synchronized {
    val encrypted = Base64.getEncoder.encodeToString(value.getBytes("UTF-8"))
    val root = loadJson()
    root.put(key, encrypted)
    saveJson(root)
  }

  def loadPassword(key: String): String = synchronized {
    if (!Files.exists(path)) return null
    val root = loadJson()
    if (!root.has(key)) return null
    val enc = root.get(key).asText()
    new String(Base64.getDecoder.decode(enc), "UTF-8")
  }
}

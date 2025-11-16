package com.anjunar.jcommander.security

import java.nio.file.{Files, Path}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

import java.util.Base64
import scala.collection.mutable

class PasswordStore(storePath: Path, keyPath: Path) {
  private val m = new ObjectMapper
  private val keyStore = new KeyStore(keyPath)

  def savePassword(host: String, password: String): Unit = {
    val key = keyStore.loadOrCreateKey()
    val map = load()
    val enc = keyStore.encrypt(key, password.getBytes)
    val node = m.createObjectNode()
    node.put("nonce", Base64.getEncoder.encodeToString(enc.nonce))
    node.put("cipher", Base64.getEncoder.encodeToString(enc.cipher))
    map.put(host, node)
    val root = m.createObjectNode()
    map.foreach(e => root.set(e._1, e._2))
    Files.createDirectories(storePath.getParent)
    m.writerWithDefaultPrettyPrinter().writeValue(storePath.toFile, root)
  }

  def loadPassword(host: String): String = {
    val key = keyStore.loadOrCreateKey()
    val map = load()
    if (!map.contains(host)) return null
    val n = map(host)
    val nonce = Base64.getDecoder.decode(n.get("nonce").asText())
    val cipher = Base64.getDecoder.decode(n.get("cipher").asText())
    new String(keyStore.decrypt(key, nonce, cipher))
  }

  private def load(): mutable.Map[String,ObjectNode] = {
    if (!Files.exists(storePath)) return mutable.Map.empty
    val root = m.readTree(storePath.toFile)
    val it = root.fieldNames()
    var out = mutable.Map.empty[String,ObjectNode]
    while (it.hasNext) {
      val k = it.next()
      out = out + (k -> root.get(k).asInstanceOf[ObjectNode])
    }
    out
  }
}

package com.anjunar.jcommander.security

import javax.crypto.{Cipher, KeyGenerator, SecretKey}
import javax.crypto.spec.{GCMParameterSpec, SecretKeySpec}
import java.nio.file.{Files, Path}
import java.security.SecureRandom

class KeyStore(keyPath: Path) {
  def loadOrCreateKey(): SecretKey = {
    if (Files.exists(keyPath)) {
      val enc = Files.readAllBytes(keyPath)
      val dec = DPAPI.unprotect(enc)
      new SecretKeySpec(dec, "AES")
    } else {
      val raw = Array.ofDim[Byte](32)
      new SecureRandom().nextBytes(raw)
      val enc = DPAPI.protect(raw)
      Files.createDirectories(keyPath.getParent)
      Files.write(keyPath, enc)
      new SecretKeySpec(raw, "AES")
    }
  }

  def encrypt(key: SecretKey, data: Array[Byte]): Encrypted = {
    val nonce = Array.ofDim[Byte](32)
    new SecureRandom().nextBytes(nonce)
    val c = Cipher.getInstance("AES/GCM/NoPadding")
    c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, nonce))
    Encrypted(nonce, c.doFinal(data))
  }

  def decrypt(key: SecretKey, nonce: Array[Byte], cipher: Array[Byte]): Array[Byte] = {
    val c = Cipher.getInstance("AES/GCM/NoPadding")
    c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, nonce))
    c.doFinal(cipher)
  }
}

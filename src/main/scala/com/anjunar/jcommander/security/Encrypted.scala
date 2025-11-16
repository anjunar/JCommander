package com.anjunar.jcommander.security

case class Encrypted(nonce: Array[Byte], cipher: Array[Byte])
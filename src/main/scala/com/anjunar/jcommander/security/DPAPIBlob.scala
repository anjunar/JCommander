package com.anjunar.jcommander.security

import com.sun.jna.{Pointer, Structure}

class DPAPIBlob extends Structure {
  var cbData: Int = 0
  var pbData: Pointer = _
  override def getFieldOrder = java.util.Arrays.asList("cbData","pbData")
}


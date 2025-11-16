package com.anjunar.jcommander.security

import com.sun.jna.{Library, Memory, Native, Pointer, PointerType, Structure}
import com.sun.jna.ptr.PointerByReference
import java.util

object DPAPI {
  trait Crypt32 extends Library {
    def CryptProtectData(pDataIn: DATA_BLOB, szDataDescr: Pointer, pOptionalEntropy: DATA_BLOB, pvReserved: Pointer, pPromptStruct: Pointer, dwFlags: Int, pDataOut: DATA_BLOB): Boolean

    def CryptUnprotectData(pDataIn: DATA_BLOB, ppszDataDescr: PointerByReference, pOptionalEntropy: DATA_BLOB, pvReserved: Pointer, pPromptStruct: Pointer, dwFlags: Int, pDataOut: DATA_BLOB): Boolean
  }

  private val inst = Native.load("Crypt32", classOf[Crypt32]).asInstanceOf[Crypt32]

  def protect(data: Array[Byte]): Array[Byte] = {
    val in = new DATA_BLOB
    in.cbData = data.length
    in.pbData = new Memory(data.length.toLong)
    in.pbData.write(0, data, 0, data.length)
    in.write()

    val out = new DATA_BLOB

    val ok = inst.CryptProtectData(in, null, null, null, null, 0, out)
    if (!ok) throw new RuntimeException("CryptProtectData failed")
    out.read()
    val res = out.pbData.getByteArray(0, out.cbData)
    res
  }

  def unprotect(data: Array[Byte]): Array[Byte] = {
    val in = new DATA_BLOB
    in.cbData = data.length
    in.pbData = new Memory(data.length.toLong)
    in.pbData.write(0, data, 0, data.length)
    in.write()

    val out = new DATA_BLOB
    val descr = new PointerByReference

    val ok = inst.CryptUnprotectData(in, descr, null, null, null, 0, out)
    if (!ok) throw new RuntimeException("CryptUnprotectData failed")
    out.read()
    out.pbData.getByteArray(0, out.cbData)
  }
}

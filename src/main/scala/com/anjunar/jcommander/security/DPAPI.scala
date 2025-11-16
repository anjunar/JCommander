package com.anjunar.jcommander.security

import com.sun.jna.{Library, Memory, Native, Pointer}
import com.sun.jna.Structure

object DPAPI {
  trait Crypt32 extends Library {
    def CryptProtectData(in: DPAPIBlob, d: String, opt: DPAPIBlob, r: Pointer, p: Pointer, f: Int, out: DPAPIBlob): Boolean
    def CryptUnprotectData(in: DPAPIBlob, d: Pointer, opt: DPAPIBlob, r: Pointer, p: Pointer, f: Int, out: DPAPIBlob): Boolean
  }

  val crypt = Native.load("Crypt32", classOf[Crypt32])

  def protect(data: Array[Byte]): Array[Byte] = {
    val in = new DPAPIBlob
    in.cbData = data.length
    in.pbData = new Memory(data.length)
    in.pbData.write(0, data, 0, data.length)
    val out = new DPAPIBlob
    crypt.CryptProtectData(in, null, null, null, null, 0, out)
    out.pbData.getByteArray(0, out.cbData)
  }

  def unprotect(data: Array[Byte]): Array[Byte] = {
    val in = new DPAPIBlob
    in.cbData = data.length
    in.pbData = new Memory(data.length)
    in.pbData.write(0, data, 0, data.length)
    val out = new DPAPIBlob
    crypt.CryptUnprotectData(in, null, null, null, null, 0, out)
    out.pbData.getByteArray(0, out.cbData)
  }
}

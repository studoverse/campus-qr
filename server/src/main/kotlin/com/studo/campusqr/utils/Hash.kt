package com.studo.campusqr.utils

import org.apache.commons.codec.binary.Hex
import java.security.MessageDigest

fun ByteArray.sha256(): String {
  val md = MessageDigest.getInstance("SHA-256")
  val hex = md.digest(this)
  return Hex.encodeHexString(hex)
}

fun String.sha256(): String {
  return this.toByteArray().sha256()
}
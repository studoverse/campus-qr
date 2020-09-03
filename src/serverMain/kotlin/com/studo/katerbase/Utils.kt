package com.studo.katerbase

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import org.apache.commons.codec.binary.Hex
import org.slf4j.impl.StaticLoggerBinder
import java.security.MessageDigest

internal fun setLogLevel(name: String, level: Level) {
  (StaticLoggerBinder.getSingleton().loggerFactory.getLogger(name) as Logger).level = level
}

fun ByteArray.sha256(): String {
  val md = MessageDigest.getInstance("SHA-256")
  val hex = md.digest(this)
  return Hex.encodeHexString(hex)
}

fun String.sha256(): String {
  return this.toByteArray().sha256()
}
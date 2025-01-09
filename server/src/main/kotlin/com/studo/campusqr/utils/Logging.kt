package com.studo.campusqr.utils

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import org.slf4j.LoggerFactory

fun setLogLevel(name: String, level: Level) {
  (LoggerFactory.getILoggerFactory() as? LoggerContext)?.getLogger(name)?.apply {
    this.level = level
  } ?: throw IllegalStateException("Failed to set log level: Invalid logger context or logger.")
}
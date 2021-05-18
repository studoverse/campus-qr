package com.studo.campusqr.utils

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import org.slf4j.impl.StaticLoggerBinder

fun setLogLevel(name: String, level: Level) {
  (StaticLoggerBinder.getSingleton().loggerFactory.getLogger(name) as Logger).level = level
}
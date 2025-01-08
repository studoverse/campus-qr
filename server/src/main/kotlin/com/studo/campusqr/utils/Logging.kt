package com.studo.campusqr.utils

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import org.slf4j.impl.StaticLoggerBinder

//import ch.qos.logback.classic.LoggerContext
//import org.slf4j.LoggerFactory

fun setLogLevel(name: String, level: Level) {
  (StaticLoggerBinder.getSingleton().loggerFactory.getLogger(name) as Logger).level = level
}

/*fun setLogLevel(name: String, level: Level) {
  val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
  val logger = loggerContext.getLogger(name) as Logger
  logger.level = level
}*/
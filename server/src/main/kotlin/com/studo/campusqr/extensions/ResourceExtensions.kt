package com.studo.campusqr.extensions

import com.studo.campusqr.Server

fun getResourceAsStream(path: String) = Server::class.java.getResourceAsStream(path) ?: null

fun getDirectoryContentPath(path: String): List<String>? = getResourceAsStream(path)
  ?.bufferedReader()?.lines()?.toList()
  ?.map { "$path/$it" }
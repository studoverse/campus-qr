package com.studo.campusqr.extensions

import com.studo.campusqr.Server

fun getResourceAsStream(path: String) = Server::class.java.getResourceAsStream(path) ?: null

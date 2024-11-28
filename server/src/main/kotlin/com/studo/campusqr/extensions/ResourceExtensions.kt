package com.studo.campusqr.extensions

import com.studo.campusqr.Server
import java.io.File
import java.net.URLDecoder

fun getResourceAsStream(path: String) = Server::class.java.getResourceAsStream(path) ?: null

fun getDirectoryContentPath(path: String): List<String>? {
  val resource = Server::class.java.getResource(path) ?: return null
  return if (resource.protocol == "file") {
    // Development: List files in the directory
    File(resource.toURI()).listFiles()?.filter { it.isFile }?.map { it.name }
  } else if (resource.protocol == "jar") {
    // Production: List files inside the JAR
    val jarFilePath = resource.path
      .substringBefore("!")
      .substringAfter("file:")
    val jarFile = java.util.jar.JarFile(URLDecoder.decode(jarFilePath, "UTF-8"))
    val entries = jarFile.entries()
    val files = mutableListOf<String>()
    while (entries.hasMoreElements()) {
      val entry = entries.nextElement()
      if (entry.name.startsWith(path.removePrefix("/")) && !entry.isDirectory) {
        // The JarFile provides a complete list of all entries in the archive, so we make sure to only add files that are in [path].
        files.add("/" + entry.name) // Add leading '/' for consistency
      }
    }
    files
  } else {
    throw IllegalStateException("Protocol is unsupported. Only file and jar are supported.")
  }
}
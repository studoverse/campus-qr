import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

// Get the current user's home directory
val userHome = System.getProperty("user.home")

// Define the path to the JetBrains folder
val jetBrainsDir = File("$userHome/Library/Application Support/JetBrains")

// Function to find the latest IntelliJ version folder
fun findLatestIntelliJVersion(): File? {
  if (jetBrainsDir.exists()) {
    // List all IntelliJ-related folders
    val ideaFolders = jetBrainsDir.listFiles { file ->
      file.isDirectory && file.name.startsWith("IntelliJIdea")
    }?.sortedByDescending { it.name }

    if (!ideaFolders.isNullOrEmpty()) {
      // Return the folder with the highest version
      return ideaFolders.firstOrNull()
    }
  }
  return null
}

// Find the latest IntelliJ version folder
val latestIntelliJFolder = findLatestIntelliJVersion()

if (latestIntelliJFolder != null) {
  // Construct the fileTemplates and liveTemplates paths based on the latest folder
  val fileTemplatesDir = File(latestIntelliJFolder, "fileTemplates")
  val liveTemplatesDir = File(latestIntelliJFolder, "templates")

  println("Detected latest IntelliJ version folder: ${latestIntelliJFolder.name}")
  println("File Templates Directory: $fileTemplatesDir")
  println("Live Templates Directory: $liveTemplatesDir")

  // Project directories (inside your project's .idea folder)
  val projectFileTemplatesDir = File(".idea/fileTemplates")
  val projectLiveTemplatesDir = File(".idea/liveTemplates")

  // Function to copy a directory recursively
  fun copyDirectory(source: File, target: File) {
    source.walkTopDown().forEach { file ->
      val targetFile = File(target, file.relativeTo(source).path)
      if (file.isDirectory) {
        targetFile.mkdirs()
      } else {
        Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
      }
    }
  }

  // Ensure IntelliJ fileTemplates directory exists
  if (!fileTemplatesDir.exists()) {
    println("Creating IntelliJ fileTemplates directory at ${fileTemplatesDir.path}")
    fileTemplatesDir.mkdirs()
  }

  // Ensure IntelliJ liveTemplates directory exists
  if (!liveTemplatesDir.exists()) {
    println("Creating IntelliJ liveTemplates directory at ${liveTemplatesDir.path}")
    liveTemplatesDir.mkdirs()
  }

  // Copy file templates from the project to IntelliJ config
  if (projectFileTemplatesDir.exists()) {
    println("Copying file templates from ${projectFileTemplatesDir.path} to ${fileTemplatesDir.path}")
    copyDirectory(projectFileTemplatesDir, fileTemplatesDir)
  } else {
    println("No file templates found at ${projectFileTemplatesDir.path}")
  }

  // Copy live templates from the project to IntelliJ config
  if (projectLiveTemplatesDir.exists()) {
    println("Copying live templates from ${projectLiveTemplatesDir.path} to ${liveTemplatesDir.path}")
    copyDirectory(projectLiveTemplatesDir, liveTemplatesDir)
  } else {
    println("No live templates found at ${projectLiveTemplatesDir.path}")
  }

  println("Templates installation completed.")

} else {
  println("No IntelliJ IDEA installation found in the JetBrains directory.")
}
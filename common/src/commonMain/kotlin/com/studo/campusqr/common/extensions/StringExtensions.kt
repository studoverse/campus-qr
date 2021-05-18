package com.studo.campusqr.common.extensions

/**
 * Basic regexes to validate a broad plausibility of certain string types.
 */

val emailRegex: Regex = Regex("[^,;:<>(){}\\[\\]\"'\\s@]+@[^,;:<>(){}\\[\\]\"'\\s@]+\\.[^,;:<>(){}\\[\\]\"'\\s@]+")

val nameRegex: Regex = Regex("[^a-z0-9.].{1,20} [^a-z0-9.].{1,20}")

val urlRegex = Regex("^(?:http(s)?://).+")

/** Returns the string if it is not empty, or null otherwise. */
fun String.emptyToNull(): String? = if (this.isEmpty()) null else this

/**
 * Very simple string formatter. It replaces %s occurrences with given args.
 * See also RBuilder.format(text: String, vararg parameters: RBuilder.() -> Unit)
 */
fun String.format(vararg parameters: String): String {
  val splitText: List<String> = this.split("%s")
  if (splitText.count() - 1 != parameters.count()) {
    throw IllegalStateException("Number of given parameters and occurrences of '%s' doesn't match!")
  }

  val resultBuilder = StringBuilder()
  parameters.forEachIndexed { index, parameter ->
    resultBuilder.append(splitText[index] + parameter)
  }
  resultBuilder.append(splitText.last())

  return resultBuilder.toString()
}
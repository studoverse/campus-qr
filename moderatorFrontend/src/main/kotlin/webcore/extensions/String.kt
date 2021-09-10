package webcore.extensions

import kotlinx.browser.document
import org.w3c.dom.HTMLTextAreaElement

val emailRegex = Regex("[^,;:<> ]+@[^,;:<> ]+\\.[^,;:<> ]+")

val nameRegex = Regex("[^a-z0-9.].{1,20} [^a-z0-9.].{1,20}")

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

fun String.copyToClipboard() {
  val textArea = document.createElement("textarea") as HTMLTextAreaElement
  textArea.value = this
  textArea.style.top = "0"
  textArea.style.left = "0"
  textArea.style.position = "fixed"
  document.body?.appendChild(textArea)
  textArea.focus()
  textArea.select()
  document.execCommand("copy")
  document.body?.removeChild(textArea)
}

external fun decodeURIComponent(encodedUriComponent: String): String
external fun encodeURIComponent(uriComponent: String): String
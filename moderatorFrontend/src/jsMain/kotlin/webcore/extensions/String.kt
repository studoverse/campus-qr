package webcore.extensions

import js.array.asList
import js.errors.toThrowable
import mui.material.paperClasses
import web.cssom.ClassName
import web.dom.document
import web.html.HTMLTextAreaElement
import web.navigator.navigator

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

fun String.copyToClipboard(onSuccess: (() -> Unit)? = null, onFail: ((throwable: Throwable) -> Unit)? = null) {
  // Check for navigator.clipboard.writeText support
  @Suppress("UnsafeCastFromDynamic")
  if (navigator.clipboard.asDynamic().writeText) {
    navigator.clipboard.writeTextAsync(this).then(
      onFulfilled = {
        onSuccess?.invoke()
      },
      onRejected = { throwable ->
        onFail?.invoke(throwable.toThrowable())
      },
    )
  } else {
    // Some old browsers (including Safari up to iOS 13) don't support navigator.clipboard.writeText
    // https://caniuse.com/mdn-api_clipboard_writetext
    // Use execCommand in this case but only for older browsers since it is deprecated.
    // Below code from https://stackoverflow.com/a/30810322/16739173
    val textArea = document.createElement("textarea") as HTMLTextAreaElement
    textArea.style.top = "0"
    textArea.style.left = "0"
    textArea.style.position = "fixed"
    textArea.style.width = "2em"
    textArea.style.height = "2em"
    textArea.style.opacity = "0"
    textArea.style.zIndex = "-1"
    textArea.value = this
    // In order to copy, we need to insert the textarea, focus it, and execCommand("copy").
    // If we are showing a modal component (Dialog, Drawer, Menu, Popover),
    // we need to insert the textarea within the Focus Trap of the modal, or else focusing it will not work.
    val focusableElement = document
      .getElementsByClassName(ClassName("MuiModal-root"))
      .asList()
      .singleOrNull { it.ariaHidden != true.toString() } // Try to find active modal, which doesn't have ariaHidden set
      ?.getElementsByClassName(paperClasses.root)?.get(0) // Paper is present in all components that use a Modal
      ?: document.body // Default case when we are not showing a modal component
    focusableElement.appendChild(textArea)
    textArea.focus()
    textArea.select()
    try {
      if (document.asDynamic().execCommand("copy")) {
        onSuccess?.invoke()
      } else {
        onFail?.invoke(Throwable(message = "Copy to clipboard failed"))
      }
    } catch (exception: Exception) {
      onFail?.invoke(exception)
    }
    focusableElement.removeChild(textArea)
  }
}

external fun encodeURI(uri: String): String
external fun decodeURI(encodedUri: String): String
external fun decodeURIComponent(encodedUriComponent: String): String
external fun encodeURIComponent(uriComponent: String): String
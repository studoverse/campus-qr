package webcore.extensions

import react.ChildrenBuilder

/**
 * Used for rendering text with one or multiple paramaters inside (for example: Paragraph with a hyperlink inside).
 * See also String.format(vararg parameters: String)
 *
 * [text] Has to contain as much "%s" as parameters are given.
 * Each "%s" will be replaced with the parameter at the same position.
 */
fun ChildrenBuilder.format(text: String, vararg parameters: ChildrenBuilder.() -> Unit) {
  val splitText: List<String> = text.split("%s")
  if (splitText.count() - 1 != parameters.count()) {
    throw IllegalStateException("Number of given parameters and occurrences of '%s' doesn't match!")
  }

  parameters.forEachIndexed { index, parameter ->
    +splitText[index]
    parameter()
  }
  +splitText.last()
}
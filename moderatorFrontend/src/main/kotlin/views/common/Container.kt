package views.common

import kotlinx.html.DIV
import react.RBuilder
import react.dom.RDOMBuilder
import react.dom.div
import react.dom.jsStyle

// TODO: @mh ReactElement<*>
fun RBuilder.container(block: RDOMBuilder<DIV>.() -> Unit): Unit =
  div(classes = "genericContainer", block = block)

fun RBuilder.spacer(size: Int = 8) = div { attrs.jsStyle { height = size } }

fun RBuilder.horizontalSpacer(size: Int = 8) = div { attrs.jsStyle { width = size } }
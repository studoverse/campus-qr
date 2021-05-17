package views.common

import kotlinx.html.DIV
import react.RBuilder
import react.ReactElement
import react.dom.RDOMBuilder
import react.dom.div
import react.dom.jsStyle

fun RBuilder.container(block: RDOMBuilder<DIV>.() -> Unit): ReactElement =
    div(classes = "genericContainer", block = block)

fun RBuilder.spacer(size: Int = 8) = div { attrs.jsStyle { height = size } }
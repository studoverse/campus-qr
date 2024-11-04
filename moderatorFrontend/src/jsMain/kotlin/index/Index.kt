package index

import app.App
import react.create
import react.dom.client.createRoot
import web.dom.document
import web.html.HTML

@JsModule("../../importCss.js")
external fun importAllCss() // Import the JS module

fun main() {
  // Load all CSS files into the head as style tags.
  //importAllCss() // TODO: @mh Comment in again after fixing the "importCss.js not found" in production.

  val container = document.createElement(HTML.div).also { htmlDivElement ->
    document.body.appendChild(htmlDivElement)
  }
  val root = createRoot(container)
  root.render(App.create())
}

package index

import app.AppFc
import react.create
import react.dom.client.createRoot
import web.dom.document
import web.html.HTML

@JsModule("../../importCss.js")
external fun importAllCss() // Import the JS module

@JsModule("../../index.css")
external val testCss: dynamic

fun main() {
  testCss // TODO: @mh Remove
  // Call the JavaScript function to import the CSS
  importAllCss()

  val container = document.createElement(HTML.div).also { htmlDivElement ->
    document.body.appendChild(htmlDivElement)
  }
  val root = createRoot(container)
  root.render(AppFc.create())
}

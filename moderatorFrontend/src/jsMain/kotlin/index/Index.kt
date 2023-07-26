package index

import app.app
import kotlinext.js.requireAll
import kotlinext.js.require
import react.Fragment
import react.create
import react.dom.client.createRoot
import web.dom.document
import web.html.HTML

fun main() {
  requireAll(require.context("../../../../../moderatorFrontend/src/jsMain/kotlin", true, js("/\\.css$/")))
  requireAll(require.context("normalize.css", true, js("/\\.css$/")))

  val container = document.createElement(HTML.div).also { htmlDivElement ->
    document.body.appendChild(htmlDivElement)
  }
  val root = createRoot(container)
  root.render(
    children = Fragment.create {
      app()
    }
  )
}

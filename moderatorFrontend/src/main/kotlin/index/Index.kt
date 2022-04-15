package index

import app.app
import kotlinext.js.require
import kotlinext.js.requireAll
import kotlinx.browser.document
import react.Fragment
import react.create
import react.dom.client.createRoot

fun main() {
  requireAll(require.context("../../../../../moderatorFrontend/src/main/kotlin", true, js("/\\.css$/")))
  requireAll(require.context("normalize.css", true, js("/\\.css$/")))

  val container = document.getElementById("root")!!
  val root = createRoot(container)
  root.render(
    children = Fragment.create {
      app()
    }
  )
}

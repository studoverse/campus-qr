package index

import app.app
import kotlinext.js.require
import kotlinext.js.requireAll
import kotlinx.browser.document
import react.dom.render

fun main() {
  requireAll(require.context("../../../../../moderatorFrontend/src/main/kotlin", true, js("/\\.css$/")))
  requireAll(require.context("normalize.css", true, js("/\\.css$/")))

  render(document.getElementById("root")) {
    app()
  }
}
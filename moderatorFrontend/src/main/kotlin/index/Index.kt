package index

import kotlinext.js.require
import kotlinext.js.requireAll
import kotlinx.browser.document
import react.Fragment
import react.create
import react.dom.render
import views.common.renderLinearProgress

fun App() {}

fun main() {
  requireAll(require.context("../../../../../moderatorFrontend/src/main/kotlin", true, js("/\\.css$/")))
  requireAll(require.context("normalize.css", true, js("/\\.css$/")))

  val container = document.getElementById("root")!!

  val application = Fragment.create {
    renderLinearProgress(true)
    //app()
  }

  render(application, container)
}

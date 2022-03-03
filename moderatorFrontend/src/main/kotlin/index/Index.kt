package index

import kotlinext.js.require
import kotlinext.js.requireAll
import kotlinx.browser.document
import react.Fragment
import react.create
import react.dom.render
import views.common.renderMbLinearProgress

fun main() {
  requireAll(require.context("../../../../../moderatorFrontend/src/main/kotlin", true, js("/\\.css$/")))
  requireAll(require.context("normalize.css", true, js("/\\.css$/")))

  val container = document.getElementById("root")!!

  val application = Fragment.create {
    renderMbLinearProgress {
      show = true
    }

    //app()
  }

  render(application, container)
}

package index

import app.AppFc
import react.create
import react.dom.client.createRoot
import web.dom.document
import web.html.HTML

fun main() {
  // TODO: @mh Replace this with the ES modules way of doing things (this is commonjs) -> import instead of require.
  //requireAll<String>(require.context("../../../../../moderatorFrontend/src/jsMain/kotlin", true, js("/\\.css$/")))
  //requireAll<String>(require.context("normalize.css", true, js("/\\.css$/")))

  val container = document.createElement(HTML.div).also { htmlDivElement ->
    document.body.appendChild(htmlDivElement)
  }
  val root = createRoot(container)
  root.render(AppFc.create())
}

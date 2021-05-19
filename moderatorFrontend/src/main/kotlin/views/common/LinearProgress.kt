package views.common

import kotlinext.js.js
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import webcore.materialUI.linearProgress
import webcore.materialUI.withStyles

interface LinearProgressProps : RProps {
  var classes: LinearProgressClasses
  var show: Boolean
}

interface LinearProgressState : RState

/**
 * Linear progress that has a fixed height of 8px also when it's not shown.
 */
class LinearProgress : RComponent<LinearProgressProps, LinearProgressState>() {
  override fun RBuilder.render() {
    div(props.classes.holder) {
      if (props.show) {
        linearProgress {}
      }
    }
  }
}

interface LinearProgressClasses {
  var holder: String
}

private val style = { _: dynamic ->
  js {
    holder = js {
      height = 8
    }
  }
}

private val styled = withStyles<LinearProgressProps, LinearProgress>(style)

fun RBuilder.renderLinearProgress(show: Boolean) = styled {
  attrs.show = show
}
  
package views.common

import kotlinext.js.js
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.div
import webcore.materialUI.linearProgress
import webcore.materialUI.withStyles

external interface LinearProgressProps : Props {
  var classes: LinearProgressClasses
  var show: Boolean
}

external interface LinearProgressState : State

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

external interface LinearProgressClasses {
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

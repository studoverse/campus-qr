package views.common

import kotlinext.js.js
import react.*
import react.dom.html.ReactHTML.div
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
class LinearProgress : Component<LinearProgressProps, LinearProgressState>() {
  override fun render(): ReactNode {
    return div.create {
      this.className = props.classes.holder // TODO: @mh Replace withStyles styling with sx prop
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

fun renderLinearProgress(show: Boolean) = styled.create {
  this.show = show
}

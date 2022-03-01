package views.common

/*
import kotlinext.js.js
import react.*
import react.dom.html.ReactHTML.div
import webcore.materialUI.linearProgress
import webcore.materialUI.withStyles

external interface LinearProgressNewProps : Props {
  var classes: LinearProgressNewClasses
  var show: Boolean
}

external interface LinearProgressNewState : State

/**
 * Linear progress that has a fixed height of 8px also when it's not shown.
 */
class LinearProgressNew : Component<LinearProgressNewProps, LinearProgressNewState>() {
  override fun render() {
    div(props.classes.holder) {
      if (props.show) {
        linearProgress {}
      }
    }
  }
}

external interface LinearProgressNewClasses {
  var holder: String
}

private val style = { _: dynamic ->
  js {
    holder = js {
      height = 8
    }
  }
}

private val styled = withStyles<LinearProgressNewProps, LinearProgressNew>(style)

fun ChildrenBuilder.renderLinearProgressNew(show: Boolean) = styled {
  attrs.show = show
}
*/
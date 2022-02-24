package views.common

import kotlinext.js.js
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import webcore.materialUI.circularProgress
import webcore.materialUI.withStyles

external interface CenteredProgressProps : RProps {
  var classes: CenteredProgressClasses
}

class CenteredProgress : RComponent<CenteredProgressProps, RState>() {
  override fun RBuilder.render() {
    div(props.classes.centered) {
      circularProgress {}
    }
  }
}

external interface CenteredProgressClasses {
  var centered: String
}

private val style = { _: dynamic ->
  js {
    centered = js {
      display = "flex"
      alignSelf = "center"
      marginTop = "auto"
      marginBottom = "auto"
      justifyContent = "center"
    }
  }
}

val styledCenteredProgress = withStyles<CenteredProgressProps, CenteredProgress>(style)

fun RBuilder.centeredProgress() = styledCenteredProgress {}
package views.common

import kotlinext.js.js
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import webcore.materialUI.circularProgress
import webcore.materialUI.withStyles

interface CenteredProgressProps : RProps {
  var classes: CenteredProgressStyle
}

class CenteredProgress : RComponent<CenteredProgressProps, RState>() {
  override fun RBuilder.render() {
    div(props.classes.centered) {
      circularProgress {}
    }
  }
}

interface CenteredProgressStyle : RProps {
  var centered: String
}

private val styles = { theme: dynamic ->
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

val styledCenteredProgress = withStyles<CenteredProgressProps, CenteredProgress>(styles)

fun RBuilder.centeredProgress() = styledCenteredProgress {}
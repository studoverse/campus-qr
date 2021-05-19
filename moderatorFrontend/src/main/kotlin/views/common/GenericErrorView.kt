package views.common

import kotlinext.js.js
import kotlinx.browser.window
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.code
import react.dom.div
import react.dom.span
import util.Strings
import util.get
import webcore.materialUI.typography
import webcore.materialUI.withStyles

interface GenericErrorViewProps : RProps {
  var title: String
  var subtitle: String
  var classes: PathNotFoundClasses
}

class PathNotFound : RComponent<GenericErrorViewProps, RState>() {
  override fun RBuilder.render() {
    div(props.classes.centeredDiv) {
      typography {
        attrs.className = props.classes.centeredText
        attrs.variant = "h1"
        +"404"
      }
      typography {
        attrs.className = props.classes.centeredText
        attrs.variant = "body1"
        +"Path \""
        span {
          code {
            +window.location.pathname
          }
        }
        +"\" doesn't seem to exist. Try something else."
      }
    }
  }
}

class GenericErrorView : RComponent<GenericErrorViewProps, RState>() {
  override fun RBuilder.render() {
    div(props.classes.centeredDiv) {
      typography {
        attrs.className = props.classes.centeredText
        attrs.variant = "h5"
        +props.title
      }
      typography {
        attrs.className = props.classes.centeredText
        attrs.variant = "body1"
        +props.subtitle
      }
    }
  }
}

interface PathNotFoundClasses {
  var centeredText: String
  var centeredDiv: String
}

private val style = { _: dynamic ->
  js {
    centeredText = js {
      paddingLeft = 16
      paddingRight = 16
      textAlign = "center"
    }
    centeredDiv = js {
      margin = "auto"
    }
  }
}

private val styledPathNotFound = withStyles<GenericErrorViewProps, PathNotFound>(style)
fun RBuilder.pathNotFoundView() = styledPathNotFound {}

private val styledGenericError = withStyles<GenericErrorViewProps, GenericErrorView>(style)
fun RBuilder.genericErrorView(title: String, subtitle: String) = styledGenericError {
  attrs.title = title
  attrs.subtitle = subtitle
}

fun RBuilder.networkErrorView() = genericErrorView(
  title = Strings.network_error.get(),
  subtitle = Strings.network_error_description.get()
)
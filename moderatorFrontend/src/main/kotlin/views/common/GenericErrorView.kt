package views.common

import csstype.Auto
import csstype.PropertiesBuilder
import csstype.TextAlign
import csstype.px
import kotlinx.browser.window
import mui.material.Box
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.ChildrenBuilder
import react.Props
import react.State
import react.dom.html.ReactHTML.code
import react.dom.html.ReactHTML.span
import react.react
import util.Strings
import util.get
import webcore.RComponent

external interface GenericErrorViewProps : Props {
  var title: String
  var subtitle: String
}

private class PathNotFound : RComponent<GenericErrorViewProps, State>() {
  override fun ChildrenBuilder.render() {
    Box {
      sx {
        margin = Auto.auto
      }
      Typography {
        sx {
          centeredText()
        }
        variant = TypographyVariant.h1
        +"404"
      }
      Typography {
        sx {
          centeredText()
        }
        variant = TypographyVariant.body1
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

private class GenericErrorView : RComponent<GenericErrorViewProps, State>() {
  override fun ChildrenBuilder.render() {
    Box {
      sx {
        margin = Auto.auto
      }
      Typography {
        sx {
          centeredText()
        }
        variant = TypographyVariant.h5
        +props.title
      }
      Typography {
        sx {
          centeredText()
        }
        variant = TypographyVariant.body1
        +props.subtitle
      }
    }
  }
}

private fun PropertiesBuilder.centeredText() {
  paddingLeft = 16.px
  paddingRight = 16.px
  textAlign = TextAlign.center
}

fun ChildrenBuilder.pathNotFoundView(title: String = "", subtitle: String = "") {
  PathNotFound::class.react {
    this.title = title
    this.subtitle = subtitle
  }
}

fun ChildrenBuilder.genericErrorView(title: String, subtitle: String) {
  GenericErrorView::class.react {
    this.title = title
    this.subtitle = subtitle
  }
}

fun ChildrenBuilder.networkErrorView() {
  genericErrorView(
    title = Strings.network_error.get(),
    subtitle = Strings.network_error_description.get()
  )
}

package views.common

import web.cssom.*
import csstype.PropertiesBuilder
import mui.material.Box
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.ChildrenBuilder
import react.FC
import react.Props
import react.State
import react.dom.html.ReactHTML.code
import react.dom.html.ReactHTML.span
import react.react
import util.Strings
import util.get
import web.location.location
import webcore.RComponent
import js.lazy.Lazy

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
            +location.pathname
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

// TODO: @mh Use this for testing
/*@Lazy
val pathNotFoundViewFc = FC<GenericErrorViewProps>("PathNotFoundView") { props ->
  PathNotFound::class.react {
    this.title = props.title
    this.subtitle = props.subtitle
  }
}*/

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

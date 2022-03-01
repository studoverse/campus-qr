package views.common

import app.GlobalCss
import app.RouteContext
import app.routeContext
import kotlinext.js.js
import react.RBuilder
import react.RComponent
import react.Props
import react.State
import react.dom.div
import util.Url
import util.toRoute
import webcore.materialUI.*

class ToolbarViewButton(
  val text: String,
  val variant: String, // outlined|contained
  val onClick: (routeContext: RouteContext) -> Unit
)

class ToolbarViewConfig(
  val title: String,
  val backButtonUrl: Url? = null,
  val buttons: List<ToolbarViewButton> = emptyList()
)

external interface ToolbarViewProps : Props {
  var classes: ToolbarViewClasses
  var config: ToolbarViewConfig
}

external interface ToolbarViewState : State

class ToolbarView : RComponent<ToolbarViewProps, ToolbarViewState>() {
  override fun RBuilder.render() {
    div(GlobalCss.flex) {
      props.config.backButtonUrl?.let { backButtonUrl ->
        routeContext.Consumer { routeContext ->
          iconButton {
            attrs.classes = js {
              root = props.classes.backButton
            }
            arrowBackIcon {}
            attrs.onClick = {
              routeContext.pushRoute(backButtonUrl.toRoute()!!)
            }
          }
        }
      }
      typography {
        attrs.className = props.classes.header
        attrs.variant = "h5"
        +props.config.title
      }
      div(GlobalCss.flexEnd) {
        routeContext.Consumer { routeContext ->
          div(props.classes.headerButtonsWrapper) {
            props.config.buttons.forEach { toolbarButton ->
              muiButton {
                attrs.classes = js {
                  root = props.classes.headerButton
                }
                attrs.variant = toolbarButton.variant
                attrs.color = "primary"
                attrs.onClick = {
                  toolbarButton.onClick(routeContext)
                }
                +toolbarButton.text
              }
            }
          }
        }
      }
    }
  }
}

external interface ToolbarViewClasses {
  var header: String
  var headerButton: String
  var headerButtonsWrapper: String
  var backButton: String
}

private val style = { _: dynamic ->
  js {
    header = js {
      margin = 16
    }
    headerButton = js {
      marginRight = 16
      marginTop = 16
      marginBottom = 16
      marginLeft = 0
    }
    headerButtonsWrapper = js {
      marginLeft = 16
    }
    backButton = js {
      width = 60
      height = 60
    }
  }
}

private val styled = withStyles<ToolbarViewProps, ToolbarView>(style)

fun RBuilder.renderToolbarView(config: ToolbarViewConfig) = styled {
  attrs.config = config
}
  
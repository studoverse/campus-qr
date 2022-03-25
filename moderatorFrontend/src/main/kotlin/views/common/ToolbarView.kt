package views.common

import app.GlobalCss
import app.RouteContext
import app.routeContext
import csstype.ClassName
import csstype.px
import kotlinx.js.jso
import mui.icons.material.ArrowBack
import mui.material.*
import mui.system.sx
import react.*
import util.Url
import util.toRoute
import webcore.RComponent

class ToolbarButton(
  val text: String,
  val variant: ButtonVariant, // outlined|contained
  val onClick: (routeContext: RouteContext) -> Unit
)

class ToolbarViewConfig(
  val title: String,
  val backButtonUrl: Url? = null,
  val buttons: List<ToolbarButton> = emptyList()
)

external interface ToolbarViewProps : Props {
  var config: ToolbarViewConfig
}

interface ToolbarViewState : State

private class ToolbarView : RComponent<ToolbarViewProps, ToolbarViewState>() {
  override fun ChildrenBuilder.render() {
    Box {
      className = ClassName(GlobalCss.flex)
      props.config.backButtonUrl?.let { backButtonUrl ->
        routeContext.Consumer {
          children = { routeContext ->
            IconButton.create {
              sx {
                width = 60.px
                height = 60.px
              }
              ArrowBack()
              onClick = {
                routeContext.pushRoute(backButtonUrl.toRoute()!!)
              }
            }
          }
        }
      }
      Typography {
        sx {
          margin = 16.px
        }
        variant = "h5"
        +props.config.title
      }
      Box {
        className = ClassName(GlobalCss.flexEnd)
        routeContext.Consumer {
          children = { routeContext ->
            Box.create {
              sx {
                marginLeft = 16.px
              }
              props.config.buttons.forEach { toolbarButton ->
                Button {
                  sx {
                    marginRight = 16.px
                    marginTop = 16.px
                    marginBottom = 16.px
                    marginLeft = 0.px
                  }
                  variant = toolbarButton.variant
                  color = ButtonColor.primary
                  onClick = {
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
}

fun ChildrenBuilder.renderToolbarView(handler: ToolbarViewProps.() -> Unit) {
  ToolbarView::class.react {
    +jso(handler)
  }
}

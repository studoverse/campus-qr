package views.common

import app.AppContext
import app.GlobalCss
import app.RouteContext
import app.appContextToInject
import csstype.ClassName
import csstype.px
import mui.icons.material.ArrowBack
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.*
import util.Url
import webcore.RComponent
import webcore.extensions.toRoute

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

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(ToolbarView::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  override fun ChildrenBuilder.render() {
    val routeContext = appContext.routeContext
    Box {
      className = ClassName(GlobalCss.flex)
      props.config.backButtonUrl?.let { backButtonUrl ->
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
      Typography {
        sx {
          margin = 16.px
        }
        variant = TypographyVariant.h5
        +props.config.title
      }
      Box {
        className = ClassName(GlobalCss.flexEnd)
        Box {
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

fun ChildrenBuilder.renderToolbarView(config: ToolbarViewConfig) {
  ToolbarView::class.react {
    this.config = config
  }
}

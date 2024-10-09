package views.common

import app.AppContext
import app.GlobalCss
import app.RouteContext
import app.appContextToInject
import web.cssom.*
import mui.icons.material.ArrowBack
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.*
import util.Url
import webcore.FcWithCoroutineScope
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

val ToolbarViewFc = FcWithCoroutineScope<ToolbarViewProps> { props, launch ->
  val appContext = useContext(appContextToInject)!!

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
            key = toolbarButton.text // Avoid duplicate key issue
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
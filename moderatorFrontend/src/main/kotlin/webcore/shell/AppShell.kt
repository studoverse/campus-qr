package webcore.shell

import kotlinext.js.js
import kotlinx.html.DIV
import react.*
import react.dom.RDOMBuilder
import react.dom.div
import webcore.materialUI.DrawerProps
import webcore.materialUI.ToolbarProps
import webcore.materialUI.withStyles

class AppShellConfig(
  var drawerList: RElementBuilder<DrawerProps>.() -> Unit,
  var viewContent: RDOMBuilder<DIV>.() -> Unit,
  var toolbarIcon: (RElementBuilder<ToolbarProps>.() -> Unit)?,
  var themeColor: String,
  var hideDrawer: Boolean,
  var smallToolbar: Boolean,
  var mobileNavOpen: Boolean,
  var appBarElevation: Int?,
  var stickyNavigation: Boolean,
)

external interface AppShellProps : RProps {
  var config: AppShellConfig
  var theme: dynamic
  var classes: dynamic
}

external interface AppShellState : RState

class AppShell(props: AppShellProps) : RComponent<AppShellProps, AppShellState>(props) {

  override fun RBuilder.render() {

    renderAppShellDrawer(
      AppShellDrawerConfig(
        props.config.mobileNavOpen,
        props.config.hideDrawer,
        props.config.drawerList,
        props.config.toolbarIcon,
        props.config.themeColor,
        props.config.smallToolbar,
        props.config.stickyNavigation,
        props.config.appBarElevation
      )
    )

    val contentClasses = "${props.classes.content}" +
        (if (!props.config.hideDrawer) " ${props.classes.drawerWidthMargin}" else "") +
        (if (props.config.smallToolbar) " ${props.classes.contentWithSmallToolbar}" else " ${props.classes.contentWithNormalToolbar}")
    div(classes = contentClasses) {
      props.config.viewContent(this)
    }
  }
}

private val styles: (dynamic) -> dynamic = { theme ->
  js {
    content = js {
      this[arrayOf(theme.breakpoints.only("sm"))] = js {
        this["min-height"] = "calc(100vh - 64px)"
      }
      this[arrayOf(theme.breakpoints.only("xs"))] = js {
        this["min-height"] = "calc(100vh - 56px)"
      }
      margin = "0 auto"
      display = "flex"
      this["flex-direction"] = "column"
    }

    contentWithNormalToolbar = js {
      this[arrayOf(theme.breakpoints.up("md"))] = js {
        this["min-height"] = "calc(100vh - 64px)"
      }
    }

    contentWithSmallToolbar = js {
      this[arrayOf(theme.breakpoints.up("md"))] = js {
        this["min-height"] = "calc(100vh - 12px)"
      }
    }

    drawerWidthMargin = js {
      this[arrayOf(theme.breakpoints.up("md"))] = js {
        marginLeft = drawerWidth
      }
    }
  }
}

val appShell: RClass<AppShellProps> = withStyles<AppShellProps, AppShell>(styles, options = js { withTheme = true })

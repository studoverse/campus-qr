package webcore.shell

import app.appContextToInject
import web.cssom.*
import mui.material.Box
import mui.system.Breakpoint
import mui.system.sx
import react.*
import webcore.FcWithCoroutineScope

class AppShellConfig(
  var drawerList: ChildrenBuilder.() -> Unit,
  var viewContent: ChildrenBuilder.() -> Unit,
  var toolbarIcon: (ChildrenBuilder.() -> Unit)?,
  var themeColor: Color,
  var hideDrawer: Boolean,
  var smallToolbar: Boolean,
  var mobileNavOpen: Boolean,
  val mobileNavOpenChange: (mobileNavOpen: Boolean) -> Unit,
  var appBarElevation: Int?,
  var stickyNavigation: Boolean,
)

external interface AppShellProps : Props {
  var config: AppShellConfig
}

val AppShell = FcWithCoroutineScope<AppShellProps> { props, launch ->
  val appContext = use(appContextToInject)!!

  val theme = appContext.theme
  AppShellDrawer {
    config = AppShellDrawerConfig(
      props.config.mobileNavOpen,
      props.config.mobileNavOpenChange,
      props.config.hideDrawer,
      props.config.drawerList,
      props.config.toolbarIcon,
      props.config.themeColor,
      props.config.smallToolbar,
      props.config.stickyNavigation,
      props.config.appBarElevation
    )
  }

  Box {
    sx {
      (theme.breakpoints.only(Breakpoint.sm)) {
        minHeight = 100.vh - 64.px
      }
      (theme.breakpoints.only(Breakpoint.xs)) {
        minHeight = 100.vh - 56.px
      }
      margin = Margin(vertical = 0.px, horizontal = Auto.auto)
      display = Display.flex
      flexDirection = FlexDirection.column

      (theme.breakpoints.up(Breakpoint.md)) {
        if (!props.config.hideDrawer) {
          marginLeft = AppShellDrawerStyles.drawerWidth.px
        }

        if (props.config.smallToolbar) {
          minHeight = 100.vh - 12.px
          marginLeft = AppShellDrawerStyles.drawerWidth.px
        } else {
          minHeight = 100.vh - 64.px
          marginLeft = AppShellDrawerStyles.drawerWidth.px
        }
      }
    }
    props.config.viewContent(this)
  }
}

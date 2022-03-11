package webcore.shell

import app.themeContext
import csstype.*
import kotlinx.js.jso
import mui.material.Box
import mui.system.Breakpoint
import mui.system.sx
import react.*
import webcore.RComponent

class AppShellConfig(
  var drawerList: ChildrenBuilder.() -> Unit,
  var viewContent: ChildrenBuilder.() -> Unit,
  var toolbarIcon: (ChildrenBuilder.() -> Unit)?,
  var themeColor: String,
  var hideDrawer: Boolean,
  var smallToolbar: Boolean,
  var mobileNavOpen: Boolean,
  var appBarElevation: Int?,
  var stickyNavigation: Boolean,
)

external interface AppShellProps : Props {
  var config: AppShellConfig
}

external interface AppShellState : State

class AppShell(props: AppShellProps) : RComponent<AppShellProps, AppShellState>(props) {

  override fun ChildrenBuilder.render() {
    themeContext.Consumer {
      children = { theme ->
        Fragment.create {
          renderAppShellDrawer {
            config = AppShellDrawerConfig(
              props.config.mobileNavOpen,
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
                  marginLeft = drawerWidth.px
                }

                if (props.config.smallToolbar) {
                  minHeight = 100.vh - 12.px
                  marginLeft = drawerWidth.px
                } else {
                  minHeight = 100.vh - 64.px
                  marginLeft = drawerWidth.px
                }
              }
            }
            props.config.viewContent(this)
          }
        }
      }
    }
  }
}

fun ChildrenBuilder.appShell(handler: AppShellProps.() -> Unit) {
  AppShell::class.react {
    +jso(handler)
  }
}

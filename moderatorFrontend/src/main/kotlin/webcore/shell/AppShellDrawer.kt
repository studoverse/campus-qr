package webcore.shell

import app.themeContext
import csstype.*
import kotlinx.js.jso
import mui.material.*
import mui.system.Breakpoint
import mui.system.sx
import react.*
import webcore.RComponent
import webcore.setState

class AppShellDrawerConfig(
  var mobileNavOpen: Boolean,
  var hideDrawer: Boolean,
  var drawerList: ChildrenBuilder.() -> Unit,
  var toolbarIcon: (ChildrenBuilder.() -> Unit)?,
  var themeColor: String,
  var smallToolbar: Boolean,
  var stickyNavigation: Boolean,
  var appBarElevation: Int?
)

external interface AppShellDrawerProps : Props {
  var config: AppShellDrawerConfig
}

external interface AppShellDrawerState : State {
  var mobileNavOpen: Boolean
}

const val drawerWidth = 240

class AppShellDrawer(props: AppShellDrawerProps) : RComponent<AppShellDrawerProps, AppShellDrawerState>(props) {

  override fun AppShellDrawerState.init(props: AppShellDrawerProps) {
    mobileNavOpen = props.config.mobileNavOpen
  }

  override fun componentWillReceiveProps(nextProps: AppShellDrawerProps) {
    setState { mobileNavOpen = nextProps.config.mobileNavOpen }
  }

  override fun ChildrenBuilder.render() {
    themeContext.Consumer {
      children = { theme ->
        Fragment.create {
          AppBar {
            if (!props.config.hideDrawer) {
              sx {
                (theme.breakpoints.up(Breakpoint.md)) {
                  marginLeft = drawerWidth.px
                  width = 100.pct - drawerWidth.px
                  if (props.config.smallToolbar) {
                    height = 12.px
                  }
                }
              }
            }
            color = AppBarColor.primary
            position = if (props.config.stickyNavigation) AppBarPosition.sticky else AppBarPosition.static
            props.config.appBarElevation?.let { elevation = it }
            Toolbar {
              sx {
                borderTop = Border(width = 6.px, style = LineStyle.solid, color = Color(props.config.themeColor))
                (theme.breakpoints.up(Breakpoint.md)) {
                  if (props.config.smallToolbar) {
                    minHeight = 12.px
                  }
                }
              }
              if (!props.config.hideDrawer) {
                IconButton {
                  sx {
                    (theme.breakpoints.up(Breakpoint.md)) {
                      display = None.none
                    }
                  }
                  mui.icons.material.Menu {
                    sx {
                      color = (Color("#fff"))
                    }
                  }
                  onClick = {
                    setState {
                      mobileNavOpen = !state.mobileNavOpen
                    }
                  }
                }
              }
              props.config.toolbarIcon?.invoke(this)
            }
          }

          if (!props.config.hideDrawer) {
            Hidden {
              smDown = true
              implementation = HiddenImplementation.css
              Drawer {
                sx {
                  drawerStyle()
                }
                open = true
                variant = DrawerVariant.permanent
                props.config.drawerList(this)
              }
            }
            Hidden {
              mdUp = true
              Drawer {
                open = state.mobileNavOpen
                variant = DrawerVariant.temporary
                onClose = { _, _ -> setState { mobileNavOpen = false } }
                ModalProps = jso {
                  keepMounted = true // Better open performance on mobile
                }
                sx {
                  drawerStyle()
                }
                SlideProps = jso {
                  // `in` = true
                  direction = SlideDirection.right
                  appear = true
                }
                props.config.drawerList(this)
              }
            }
          }
        }
      }
    }
  }

  private fun PropertiesBuilder.drawerStyle() {
    MuiDrawer.paper {
      width = drawerWidth.px
    }
  }
}

fun ChildrenBuilder.renderAppShellDrawer(handler: AppShellDrawerProps.() -> Unit) {
  AppShellDrawer::class.react {
    +jso(handler)
  }
}

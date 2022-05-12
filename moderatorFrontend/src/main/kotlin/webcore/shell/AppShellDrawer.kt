package webcore.shell

import app.AppContext
import app.appContextToInject
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
  var themeColor: Color,
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

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(AppShellDrawer::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  override fun AppShellDrawerState.init(props: AppShellDrawerProps) {
    mobileNavOpen = props.config.mobileNavOpen
  }

  override fun componentWillReceiveProps(nextProps: AppShellDrawerProps) {
    setState { mobileNavOpen = nextProps.config.mobileNavOpen }
  }

  override fun ChildrenBuilder.render() {
    val theme = appContext.theme
    val topLineHeight = 12.px
    AppBar {
      if (!props.config.hideDrawer) {
        sx {
          (theme.breakpoints.up(Breakpoint.md)) {
            marginLeft = drawerWidth.px
            width = 100.pct - drawerWidth.px
            if (props.config.smallToolbar) {
              height = topLineHeight
            }
          }
        }
      }
      color = AppBarColor.primary
      position = if (props.config.stickyNavigation) AppBarPosition.sticky else AppBarPosition.static
      props.config.appBarElevation?.let { elevation = it }
      Toolbar {
        sx {
          (theme.breakpoints.up(Breakpoint.md)) {
            // No top border for small screens because menu button is otherwise not symmetric
            borderTop = Border(width = topLineHeight, style = LineStyle.solid, color = props.config.themeColor)
            if (props.config.smallToolbar) {
              minHeight = topLineHeight
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
                color = NamedColor.white
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
      Drawer {
        sx {
          (theme.breakpoints.down(Breakpoint.md)) {
            display = None.none
          }
          (theme.breakpoints.up(Breakpoint.md)) {
            drawerStyle()
          }
        }
        open = true
        variant = DrawerVariant.permanent
        props.config.drawerList(this)
      }
      Drawer {
        open = state.mobileNavOpen
        variant = DrawerVariant.temporary
        onClose = { _, _ -> setState { mobileNavOpen = false } }
        ModalProps = jso {
          keepMounted = true // Better open performance on mobile
        }
        sx {
          (theme.breakpoints.up(Breakpoint.md)) {
            display = None.none
          }
          (theme.breakpoints.down(Breakpoint.md)) {
            drawerStyle()
          }
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

  private fun PropertiesBuilder.drawerStyle() {
    MuiDrawer.paper {
      width = drawerWidth.px
    }
  }
}

fun ChildrenBuilder.renderAppShellDrawer(config: AppShellDrawerConfig) {
  AppShellDrawer::class.react {
    this.config = config
  }
}

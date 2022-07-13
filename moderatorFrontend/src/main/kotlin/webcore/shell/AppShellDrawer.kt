package webcore.shell

import app.AppContext
import app.appContextToInject
import csstype.*
import kotlinx.browser.window
import kotlinx.js.jso
import kotlinx.js.timers.setTimeout
import mui.material.*
import mui.system.Breakpoint
import mui.system.sx
import org.w3c.dom.events.Event
import react.*
import webcore.RComponent

class AppShellDrawerConfig(
  var mobileNavOpen: Boolean,
  val mobileNavOpenChange: (mobileNavOpen: Boolean) -> Unit,
  var hideDrawer: Boolean,
  var drawerList: ChildrenBuilder.() -> Unit,
  var toolbarIcon: (ChildrenBuilder.() -> Unit)?,
  var themeColor: Color,
  var smallToolbar: Boolean,
  var stickyNavigation: Boolean,
  var appBarElevation: Int?,
)

external interface AppShellDrawerProps : Props {
  var config: AppShellDrawerConfig
}

external interface AppShellDrawerState : State

const val drawerWidth = 240

class AppShellDrawer(props: AppShellDrawerProps) : RComponent<AppShellDrawerProps, AppShellDrawerState>(props) {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(AppShellDrawer::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  private fun onResize(@Suppress("UNUSED_PARAMETER") event: Event) {
    fixDrawerIssue()
  }

  private fun fixDrawerIssue() {
    if (window.innerWidth >= appContext.theme.breakpoints.values[Breakpoint.md]!!.toInt() && props.config.mobileNavOpen) {
      console.log("Applying workaround for https://github.com/mui/material-ui/issues/32251")
      props.config.mobileNavOpenChange(false)
    }
  }

  override fun UNSAFE_componentWillMount() {
    window.addEventListener("resize", ::onResize)
  }

  override fun componentWillUnmount() {
    window.removeEventListener("resize", ::onResize)
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
              props.config.mobileNavOpenChange(!props.config.mobileNavOpen)
            }
          }
        }
        props.config.toolbarIcon?.invoke(this)
      }
    }

    if (!props.config.hideDrawer) {
      // Desktop drawer
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
      // Mobile drawer
      Drawer {
        open = props.config.mobileNavOpen
        variant = DrawerVariant.temporary
        onClose = { _, _ -> props.config.mobileNavOpenChange(false) }
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
      setTimeout(::fixDrawerIssue, 0)
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

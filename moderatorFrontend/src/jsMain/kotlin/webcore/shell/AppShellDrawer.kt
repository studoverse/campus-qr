package webcore.shell

import app.appContextToInject
import csstype.PropertiesBuilder
import web.cssom.*
import web.window.window
import js.objects.unsafeJso
import mui.material.*
import mui.system.Breakpoint
import mui.system.sx
import react.*
import web.events.Event
import web.events.RESIZE
import web.events.addEventListener
import web.events.removeEventListener
import web.timers.setTimeout
import webcore.FcWithCoroutineScope
import webcore.shell.AppShellDrawerStyles.drawerStyle

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

object AppShellDrawerStyles {
  const val drawerWidth = 240
  fun PropertiesBuilder.drawerStyle(drawerWidth: Int) {
    drawerClasses.paper {
      width = drawerWidth.px
    }
  }
}

val AppShellDrawer = FcWithCoroutineScope<AppShellDrawerProps> { props, launch ->
  val appContext = use(appContextToInject)!!

  fun fixDrawerIssue() {
    if (window.innerWidth >= appContext.theme.breakpoints.values[Breakpoint.md]!!.toInt() && props.config.mobileNavOpen) {
      console.log("Applying workaround for https://github.com/mui/material-ui/issues/32251")
      props.config.mobileNavOpenChange(false)
    }
  }

  fun onResize(@Suppress("UNUSED_PARAMETER") event: Event) {
    fixDrawerIssue()
  }

  useEffectOnceWithCleanup {
    window.addEventListener(Event.RESIZE, ::onResize)
    onCleanup {
      window.removeEventListener(Event.RESIZE, ::onResize)
    }
  }

  val theme = appContext.theme
  val topLineHeight = 12.px
  AppBar {
    if (!props.config.hideDrawer) {
      sx {
        (theme.breakpoints.up(Breakpoint.md)) {
          marginLeft = AppShellDrawerStyles.drawerWidth.px
          width = 100.pct - AppShellDrawerStyles.drawerWidth.px
          if (props.config.smallToolbar) {
            height = topLineHeight
          }
        }
      }
    }
    color = AppBarColor.primary
    position = if (props.config.stickyNavigation) AppBarPosition.sticky else AppBarPosition.static
    props.config.appBarElevation?.let { appBarElevation ->
      elevation = appBarElevation
    }
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
          drawerStyle(drawerWidth = AppShellDrawerStyles.drawerWidth)
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
      ModalProps = unsafeJso {
        keepMounted = true // Better open performance on mobile
      }
      sx {
        (theme.breakpoints.up(Breakpoint.md)) {
          display = None.none
        }
        (theme.breakpoints.down(Breakpoint.md)) {
          drawerStyle(drawerWidth = AppShellDrawerStyles.drawerWidth)
        }
      }
      SlideProps = unsafeJso {
        // `in` = true
        direction = SlideDirection.right
        appear = true
      }
      props.config.drawerList(this)
    }
    setTimeout(::fixDrawerIssue, 0)
  }
}

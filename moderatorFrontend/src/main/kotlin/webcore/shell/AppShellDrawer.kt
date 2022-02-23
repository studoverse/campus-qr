package webcore.shell

import kotlinext.js.js
import react.*
import webcore.materialUI.*

class AppShellDrawerConfig(
  var mobileNavOpen: Boolean,
  var hideDrawer: Boolean,
  var drawerList: RElementBuilder<DrawerProps>.() -> Unit,
  var toolbarIcon: (RElementBuilder<ToolbarProps>.() -> Unit)?,
  var themeColor: String,
  var smallToolbar: Boolean,
  var stickyNavigation: Boolean,
  var appBarElevation: Int?
)

// TODO: @mh Make external & refactor receiver function to normal function
interface AppShellDrawerProps : RProps {
  var classes: AppShellDrawerClasses
  var config: AppShellDrawerConfig
}

external interface AppShellDrawerState : RState {
  var mobileNavOpen: Boolean
}

// TODO: @mh Try with new kotlin milestone version if this would still work although not using an external interface
//data class AppShellDrawerState(var mobileNavOpen: Boolean)

// TODO: @mh Alternative: Use it like this to get compile time error when a state is not defined
//           https://github.com/Kotlin/full-stack-web-jetbrains-night-sample/blob/master/client/src/main/kotlin/view/Post.kt
/*fun MyAccountState() = object : MyAccountState {
  override var someBoolean: Boolean = false
}*/

const val drawerWidth = 240

class AppShellDrawer(props: AppShellDrawerProps) : RComponent<AppShellDrawerProps, AppShellDrawerState>(props) {

  override fun AppShellDrawerState.init(props: AppShellDrawerProps) {
    mobileNavOpen = props.config.mobileNavOpen
  }

  override fun componentWillReceiveProps(nextProps: AppShellDrawerProps) {
    setState { mobileNavOpen = nextProps.config.mobileNavOpen }
  }

  private val drawerStyles = js {
    this.paper = js {
      width = drawerWidth
    }
  }

  private val styledDrawer = withStyles(drawerStyles, drawer)

  private val toolbarStyles: (dynamic) -> dynamic = { theme ->
    js {
      this.root = js {
        borderTop = "6px solid ${props.config.themeColor}"

        this[arrayOf(theme.breakpoints.up("md"))] = js {
          if (props.config.smallToolbar) {
            minHeight = "12px"
          }
        }
      }
    }
  }

  private val styledToolbar = withStyles(toolbarStyles, toolbar)

  override fun RBuilder.render() {

    appBar {
      if (!props.config.hideDrawer) {
        var classes = "${props.classes.appBarStyles} ${props.classes.drawerWidthMargin}"
        if (props.config.smallToolbar) {
          classes += " ${props.classes.smallToolbar}"
        }
        attrs.className = classes
      }
      attrs.color = "primary"
      attrs.position = if (props.config.stickyNavigation) "sticky" else "static"
      props.config.appBarElevation?.let { attrs.elevation = it }
      styledToolbar {
        if (!props.config.hideDrawer) {
          iconButton {
            attrs.className = props.classes.navIconHide
            menuIcon {
              attrs.className = props.classes.menuIcon
            }
            attrs.onClick = {
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
      hidden {
        attrs.smDown = true
        attrs.implementation = "css"
        styledDrawer {
          attrs.open = true
          attrs.variant = "permanent"
          props.config.drawerList(this)
        }
      }
      hidden {
        attrs.mdUp = true
        styledDrawer {
          attrs.open = state.mobileNavOpen
          attrs.variant = "temporary"
          attrs.onClose = { setState { mobileNavOpen = false } }
          attrs.ModalProps = js {
            keepMounted = true // Better open performance on mobile
          }
          attrs.classes = js {
            this.paper = props.classes.drawerPaper
          }
          attrs.SlideProps = js {
            // `in` = true
            direction = "right"
            appear = true
          }
          props.config.drawerList(this)
        }
      }
    }
  }
}

// TODO: @mh Make these style classes also to external interfaces?
interface AppShellDrawerClasses {
  var drawerPaper: String
  var appBarStyles: String
  var drawerWidthMargin: String
  var smallToolbar: String
  var navIconHide: String
  var menuIcon: String
}

private val AppShellDrawerStyle: (dynamic) -> dynamic = { theme ->
  js {
    drawerPaper = js {
      width = drawerWidth
    }
    appBarStyles = js {
      this[arrayOf(theme.breakpoints.up("md"))] = js {
        width = "calc(100% - ${drawerWidth}px)"
      }
    }
    drawerWidthMargin = js {
      this[arrayOf(theme.breakpoints.up("md"))] = js {
        marginLeft = drawerWidth
      }
    }
    smallToolbar = js {
      this[arrayOf(theme.breakpoints.up("md"))] = js {
        height = "12px"
      }
    }
    navIconHide = js {
      this[arrayOf(theme.breakpoints.up("md"))] = js {
        display = "none"
      }
    }
    this.menuIcon = js {
      color = theme.palette.burgerMenuIconColor ?: "#fff"
    }
  }
}

private val styled =
  withStyles<AppShellDrawerProps, AppShellDrawer>(AppShellDrawerStyle, options = js { withTheme = true })

fun RBuilder.renderAppShellDrawer(config: AppShellDrawerConfig) = styled {
  attrs.config = config
}
  
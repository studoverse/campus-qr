package app

import com.studo.campusqr.common.utils.LocalizedString
import csstype.*
import mui.icons.material.Person
import mui.icons.material.SvgIconComponent
import mui.material.*
import mui.system.sx
import org.w3c.dom.HTMLAnchorElement
import react.*
import react.dom.events.MouseEvent
import react.dom.html.AnchorTarget
import react.dom.html.ReactHTML.a
import util.Strings
import util.Url
import util.get
import views.common.spacer
import views.settings.renderSettings
import webcore.LogoBadgeConfig
import webcore.RComponent
import webcore.logoBadge

class SideDrawerItem(
  val label: LocalizedString,
  val url: Url,
  val icon: SvgIconComponent
)

class AppDrawerItemsConfig(
  val loading: Boolean,
  val checkInSideDrawerItems: List<SideDrawerItem>,
  val moderatorSideDrawerItems: List<SideDrawerItem>,
  val adminSideDrawerItems: List<SideDrawerItem>,
  val mobileNavOpenChange: (mobileNavOpen: Boolean) -> Unit,
)

external interface AppDrawerItemsProps : Props {
  var config: AppDrawerItemsConfig
}

external interface AppDrawerItemsState : State

private class AppDrawerItems : RComponent<AppDrawerItemsProps, AppDrawerItemsState>() {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(AppDrawerItems::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  override fun ChildrenBuilder.render() {
    val userData = appContext.userDataContext.userData
    logoBadge(
      config = LogoBadgeConfig(
        logoUrl = "$baseUrl/static/images/logo_campusqr.png",
        logoAlt = "Campus QR",
        badgeTitle = userData?.appName ?: "",
        badgeSubtitle = userData?.clientUser?.name ?: ""
      )
    )

    if (props.config.loading) {
      LinearProgress {}
    }

    fun drawerListItem(
      label: String,
      url: String? = null,
      onClick: ((event: MouseEvent<HTMLAnchorElement, *>) -> Unit)? = null,
      icon: SvgIconComponent? = null,
      selected: Boolean = false,
      openInNewTab: Boolean = false,
      showLoadingIndicator: Boolean = false
    ) {
      if (url != null && onClick != null) {
        throw IllegalArgumentException("Either specify url or onClick")
      }

      Link {
        component = a
        sx {
          textDecoration = important(None.none)
          color = Color(ColorPalette.textDefault)
        }
        url?.let { url -> href = url }
        onClick?.let { onClickEvent ->
          this.onClick = { event ->
            onClickEvent(event)
            props.config.mobileNavOpenChange(false) // Close menu on click
          }
        }
        if (openInNewTab) {
          target = AnchorTarget._blank
        }
        rel = "noopener"
        ListItemButton {
          this.selected = selected
          ListItemIcon {
            if (showLoadingIndicator) {
              CircularProgress { size = "25px" }
            } else {
              icon?.invoke()
            }
          }
          ListItemText {
            sx {
              MuiListItemText.primary {
                fontSize = 14.px
                hyphens = Auto.auto
              }
            }
            +label
              .replace("_", "_\u00ad") // Add soft hyphen after some gender-gap characters for long gendered words
              .replace("*", "*\u00ad")
              .replace("/", "/\u00ad")
              .replace(":", ":\u00ad")
          }
        }
      }
    }

    fun drawerItems() {
      val currentAppRoute = appContext.routeContext.currentAppRoute
      if (props.config.checkInSideDrawerItems.isNotEmpty()) {
        ListSubheader {
          +Strings.check_in.get()
        }
        props.config.checkInSideDrawerItems.forEach { sideDrawerItem ->
          drawerListItem(
            label = sideDrawerItem.label.get(),
            icon = sideDrawerItem.icon,
            selected = currentAppRoute?.url == sideDrawerItem.url,
            url = sideDrawerItem.url.path
          )
        }
      }

      if (props.config.moderatorSideDrawerItems.isNotEmpty()) {
        Divider {}
        ListSubheader {
          +Strings.user_type_moderator_action.get()
        }
        props.config.moderatorSideDrawerItems.forEach { sideDrawerItem ->
          drawerListItem(
            label = sideDrawerItem.label.get(),
            icon = sideDrawerItem.icon,
            selected = currentAppRoute?.url == sideDrawerItem.url,
            url = sideDrawerItem.url.path
          )
        }
      }

      if (props.config.adminSideDrawerItems.isNotEmpty()) {
        Divider {}
        ListSubheader {
          +Strings.user_type_admin_action.get()
        }
        props.config.adminSideDrawerItems.forEach { sideDrawerItem ->
          drawerListItem(
            label = sideDrawerItem.label.get(),
            icon = sideDrawerItem.icon,
            selected = currentAppRoute?.url == sideDrawerItem.url,
            url = sideDrawerItem.url.path
          )
        }
      }

      if (appContext.userDataContext.userData?.externalAuthProvider == false) {
        Divider {}
        ListSubheader {
          +Strings.other.get()
        }
        drawerListItem(
          label = Strings.account_settings.get(),
          icon = Person,
          selected = currentAppRoute?.url == Url.ACCOUNT_SETTINGS,
          url = Url.ACCOUNT_SETTINGS.path,
        )
      }
    }

    List {
      disablePadding = true

      // Student specific features
      drawerItems()
    }

    Box {
      sx {
        flex = number(1.0)
      }
    }
    renderSettings()
    spacer(16)

  }
}

fun ChildrenBuilder.renderAppDrawerItems(config: AppDrawerItemsConfig) {
  AppDrawerItems::class.react {
    this.config = config
  }
}
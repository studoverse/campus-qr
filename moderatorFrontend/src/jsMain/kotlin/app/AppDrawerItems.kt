package app

import com.studo.campusqr.common.utils.LocalizedString
import mui.icons.material.Person
import mui.icons.material.SvgIconComponent
import mui.material.*
import mui.system.sx
import react.*
import react.dom.events.MouseEvent
import react.dom.html.ReactHTML.a
import util.Strings
import util.Url
import util.get
import views.common.spacer
import views.settings.Settings
import web.cssom.*
import web.window.WindowTarget
import web.window._blank
import webcore.FcWithCoroutineScope
import webcore.LogoBadge
import webcore.LogoBadgeConfig

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

val AppDrawerItems = FcWithCoroutineScope<AppDrawerItemsProps> { props, launch ->
  val appContext = use(appContextToInject)!!

  val userData = appContext.userDataContext.userData
  LogoBadge {
    config = LogoBadgeConfig(
      logoUrl = "$baseUrl/static/images/logo_campusqr.png",
      logoAlt = "Campus QR",
      badgeTitle = userData?.appName ?: "",
      badgeSubtitle = userData?.clientUser?.name ?: ""
    )
  }

  if (props.config.loading) {
    LinearProgress {}
  }

  fun drawerListItem(
    label: String,
    url: String? = null,
    onClick: ((event: MouseEvent<web.html.HTMLAnchorElement, *>) -> Unit)? = null,
    icon: SvgIconComponent? = null,
    selected: Boolean = false,
    openInNewTab: Boolean = false,
    showLoadingIndicator: Boolean = false
  ) {
    if (url != null && onClick != null) {
      throw IllegalArgumentException("Either specify url or onClick")
    }

    Link {
      key = label // Keys need to be unique for each entry.
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
        target = WindowTarget._blank
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
            listItemTextClasses.primary {
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
  Settings {}
  spacer(16)
}

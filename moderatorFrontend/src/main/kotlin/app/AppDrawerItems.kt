package app

import com.studo.campusqr.common.payloads.UserData
import com.studo.campusqr.common.utils.LocalizedString
import kotlinext.js.js
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.*
import react.dom.a
import react.dom.div
import react.dom.jsStyle
import util.AppRoute
import util.Strings
import util.Url
import util.get
import views.common.spacer
import views.settings.renderSettings
import webcore.logoBadge
import webcore.materialUI.*

class SideDrawerItem(
  val label: LocalizedString,
  val url: Url,
  val icon: RClass<IconProps>
)

class AppDrawerItemsConfig(
  val userData: UserData?,
  val currentAppRoute: AppRoute?,
  val loading: Boolean,
  val checkInSideDrawerItems: List<SideDrawerItem>,
  val moderatorSideDrawerItems: List<SideDrawerItem>,
  val adminSideDrawerItems: List<SideDrawerItem>,
  val onCloseMobileNav: () -> Unit
)

external interface AppDrawerItemsProps : RProps {
  var config: AppDrawerItemsConfig
  var classes: AppDrawerClasses
}

external interface AppDrawerItemsState : RState

class AppDrawerItems : RComponent<AppDrawerItemsProps, AppDrawerItemsState>() {
  override fun RBuilder.render() {
    logoBadge(
      logoUrl = "$baseUrl/static/images/logo_campusqr.png",
      logoAlt = "Campus QR",
      badgeTitle = props.config.userData?.appName ?: "",
      badgeSubtitle = props.config.userData?.clientUser?.name ?: ""
    )

    if (props.config.loading) {
      linearProgress {}
    }

    fun drawerListItem(
      label: String,
      url: String? = null,
      onClick: ((event: Event) -> Unit)? = null,
      icon: RClass<IconProps>? = null,
      selected: Boolean = false,
      openInNewTab: Boolean = false,
      showLoadingIndicator: Boolean = false
    ) {
      if (url != null && onClick != null) {
        throw IllegalArgumentException("Either specify url or onClick")
      }

      a(classes = props.classes.drawerLink) {
        url?.let { url -> attrs.href = url }
        onClick?.let { onClick ->
          attrs.onClickFunction = { event ->
            onClick(event)
            props.config.onCloseMobileNav()
          }
        }
        if (openInNewTab) {
          attrs.target = "_blank"
        }
        attrs.rel = "noopener"
        listItem {
          attrs.selected = selected
          attrs.button = true
          listItemIcon {
            if (showLoadingIndicator) {
              circularProgress { attrs.size = "25px" }
            } else {
              icon?.invoke { }
            }
          }
          listItemText {
            attrs.classes = js {
              primary = props.classes.drawerItemText
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
      if (props.config.checkInSideDrawerItems.isNotEmpty()) {
        listSubheader {
          +Strings.check_in.get()
        }
        props.config.checkInSideDrawerItems.forEach { sideDrawerItem ->
          drawerListItem(
            label = sideDrawerItem.label.get(),
            icon = sideDrawerItem.icon,
            selected = props.config.currentAppRoute?.url == sideDrawerItem.url,
            url = sideDrawerItem.url.path
          )
        }
      }

      if (props.config.moderatorSideDrawerItems.isNotEmpty()) {
        divider {}
        listSubheader {
          +Strings.user_type_moderator_action.get()
        }
        props.config.moderatorSideDrawerItems.forEach { sideDrawerItem ->
          drawerListItem(
            label = sideDrawerItem.label.get(),
            icon = sideDrawerItem.icon,
            selected = props.config.currentAppRoute?.url == sideDrawerItem.url,
            url = sideDrawerItem.url.path
          )
        }
      }

      if (props.config.adminSideDrawerItems.isNotEmpty()) {
        divider {}
        listSubheader {
          +Strings.user_type_admin_action.get()
        }
        props.config.adminSideDrawerItems.forEach { sideDrawerItem ->
          drawerListItem(
            label = sideDrawerItem.label.get(),
            icon = sideDrawerItem.icon,
            selected = props.config.currentAppRoute?.url == sideDrawerItem.url,
            url = sideDrawerItem.url.path
          )
        }
      }

      if (props.config.userData?.externalAuthProvider == false) {
        divider {}
        listSubheader {
          +Strings.other.get()
        }
        drawerListItem(
          label = Strings.account_settings.get(),
          icon = personIcon,
          selected = props.config.currentAppRoute?.url == Url.ACCOUNT_SETTINGS,
          url = Url.ACCOUNT_SETTINGS.path
        )
      }
    }

    list {
      attrs.disablePadding = true

      // Student specific features
      drawerItems()
    }

    div {
      attrs.jsStyle {
        flex = "1"
      }
    }
    renderSettings()
    spacer(16)
  }
}

interface AppDrawerClasses {
  val drawerLink: String
  val drawerItemText: String
}

private val style = { _: dynamic ->
  js {
    drawerLink = js {
      textDecoration = "none!important"
      color = ColorPalette.textDefault
    }
    drawerItemText = js {
      fontSize = "14px"
      hyphens = "auto"
    }
  }
}

private val styled = withStyles<AppDrawerItemsProps, AppDrawerItems>(style)

fun RBuilder.renderAppDrawerItems(config: AppDrawerItemsConfig) = styled {
  attrs.config = config
}
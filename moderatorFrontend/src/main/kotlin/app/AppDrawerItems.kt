package app

import com.studo.campusqr.common.payloads.UserData
import com.studo.campusqr.common.utils.LocalizedString
import csstype.*
import kotlinx.js.jso
import mui.icons.material.Person
import mui.icons.material.SvgIconComponent
import mui.material.*
import mui.system.sx
import org.w3c.dom.HTMLAnchorElement
import react.*
import react.dom.events.MouseEvent
import react.dom.html.AnchorTarget
import react.dom.html.ReactHTML.a
import util.AppRoute
import util.Strings
import util.Url
import util.get
import views.common.spacer
import views.settings.renderSettings

class SideDrawerItem(
  val label: LocalizedString,
  val url: Url,
  val icon: SvgIconComponent
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

external interface AppDrawerItemsProps : Props {
  var config: AppDrawerItemsConfig
}

external interface AppDrawerItemsState : State

class AppDrawerItems : Component<AppDrawerItemsProps, AppDrawerItemsState>() {
  override fun render(): ReactNode {
    return Fragment.create {
      // TODO: @mh Add LogoBadge.kt file
      /*logoBadge(
        logoUrl = "$baseUrl/static/images/logo_campusqr.png",
        logoAlt = "Campus QR",
        badgeTitle = props.config.userData?.appName ?: "",
        badgeSubtitle = props.config.userData?.clientUser?.name ?: ""
      )*/

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

        mui.material.Link {
          component = a
          sx {
            textDecoration = important(None.none)
            color = Color(ColorPalette.textDefault)
          }
          url?.let { url -> href = url }
          onClick?.let { onClickEvent ->
            this.onClick = { event ->
              onClickEvent(event)
              props.config.onCloseMobileNav()
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
                icon?.invoke { }
              }
            }
            ListItemText {
              sx {
                fontSize = 14.px
                hyphens = Auto.auto
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
          ListSubheader {
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
          Divider {}
          ListSubheader {
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
          Divider {}
          ListSubheader {
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
          Divider {}
          ListSubheader {
            +Strings.other.get()
          }
          drawerListItem(
            label = Strings.account_settings.get(),
            icon = Person,
            selected = props.config.currentAppRoute?.url == Url.ACCOUNT_SETTINGS,
            url = Url.ACCOUNT_SETTINGS.path,
          )
        }
      }

      mui.material.List {
        disablePadding = true

        // Student specific features
        drawerItems()
      }

      Box {
        sx {
          flex = Flex(number(1.0), number(1.0), 0.px)
        }
      }
      renderSettings()
      spacer(16)
    }
  }
}

fun ChildrenBuilder.renderAppDrawerItems(handler: AppDrawerItemsProps.() -> Unit) {
  AppDrawerItems::class.react {
    +jso(handler)
  }
}
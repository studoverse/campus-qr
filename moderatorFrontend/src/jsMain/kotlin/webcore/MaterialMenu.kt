package webcore

import js.lazy.Lazy
import web.cssom.*
import mui.icons.material.MoreVert
import mui.icons.material.SvgIconComponent
import mui.material.*
import mui.system.sx
import web.dom.Element
import web.events.Event
import react.Props
import react.dom.aria.AriaHasPopup
import react.dom.aria.ariaHasPopup
import react.dom.aria.ariaLabel
import react.dom.aria.ariaOwns
import react.useState
import webcore.extensions.randomNumberString
import kotlin.random.Random

class MenuItem(
  val text: String,
  val enabled: Boolean = true,
  val icon: SvgIconComponent? = null,
  val onClick: () -> Unit
)

class MaterialMenuConfig(
  var className: String = "",
  var fontSize: SvgIconSize? = null,
  var menuItems: List<MenuItem>,
)

external interface MaterialMenuProps : Props {
  var config: MaterialMenuConfig
}

//@Lazy
val MaterialMenu = FcWithCoroutineScope<MaterialMenuProps> { props, launch ->
  var open: Boolean by useState(false)
  var anchorEl: Element? by useState(null)

  val ariaId = "name-menu-${Random.randomNumberString()}"

  IconButton {
    if (open) ariaOwns = ariaId
    ariaHasPopup = AriaHasPopup.`true`
    onClick = { event ->
      val target = event.currentTarget
      open = !open
      anchorEl = target

      event.preventDefault()
      event.stopPropagation()
    }
    ariaLabel = "More"
    MoreVert {
      if (props.config.fontSize != null) {
        fontSize = props.config.fontSize!!
      }
    }
  }
  Menu {
    onClose = { event: Event ->
      open = false
      anchorEl = null

      event.preventDefault()
      event.stopPropagation()
    }
    id = ariaId
    this.open = open
    anchorEl?.let { anchorElement ->
      this.anchorEl = { anchorElement }
    }
    props.config.menuItems.forEach { item ->
      MenuItem {
        item.icon?.let { icon ->
          ListItemIcon {
            sx {
              minWidth = 36.px
            }
            icon()
          }
        }
        sx {
          fontSize = 14.px
        }
        disabled = !item.enabled
        onClick = {
          item.onClick()
          open = false
        }
        +item.text
      }
    }
  }
}

package webcore.materialMenu

import js.lazy.Lazy
import web.cssom.*
import mui.icons.material.MoreVert
import mui.material.*
import mui.system.sx
import react.Props
import react.dom.aria.AriaHasPopup
import react.dom.aria.ariaHasPopup
import react.dom.aria.ariaLabel
import react.dom.aria.ariaOwns
import webcore.FcWithCoroutineScope
import webcore.extensions.randomNumberString
import kotlin.random.Random

external interface MaterialMenuProps : Props {
  var config: MaterialMenuConfig
}

@Lazy
val MaterialMenu = FcWithCoroutineScope<MaterialMenuProps> { props, launch ->
  val controller: MaterialMenuController = MaterialMenuController.useMaterialMenuController(launch = launch)

  val ariaId = "name-menu-${Random.randomNumberString()}"

  IconButton {
    if (controller.open) ariaOwns = ariaId
    ariaHasPopup = AriaHasPopup.`true`
    onClick = controller.onOpenMenuClick
    ariaLabel = "More"
    MoreVert {
      if (props.config.fontSize != null) {
        fontSize = props.config.fontSize!!
      }
    }
  }
  Menu {
    onClose = controller.onMenuClose
    id = ariaId
    this.open = open
    controller.anchorEl?.let { anchorElement ->
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
          controller.onItemClick(item)
        }
        +item.text
      }
    }
  }
}

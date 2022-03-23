package webcore

import csstype.px
import kotlinx.js.jso
import mui.icons.material.MoreVert
import mui.icons.material.SvgIconComponent
import mui.material.IconButton
import mui.material.ListItemIcon
import mui.material.Menu
import mui.material.MenuItem
import mui.system.sx
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget
import react.ChildrenBuilder
import react.Props
import react.State
import react.dom.aria.AriaHasPopup
import react.dom.aria.ariaHasPopup
import react.dom.aria.ariaLabel
import react.dom.aria.ariaOwns
import react.react
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
  var fontSize: String? = null,
  var menuItems: List<MenuItem>,
)

external interface MaterialMenuProps : Props {
  var config: MaterialMenuConfig
}

external interface MaterialMenuState : State {
  var open: Boolean
  var anchorEl: EventTarget?
  var ariaId: String
}

private class MaterialMenu : RComponent<MaterialMenuProps, MaterialMenuState>() {

  override fun MaterialMenuState.init() {
    open = false
    anchorEl = null
    ariaId = Random.randomNumberString()
  }

  override fun ChildrenBuilder.render() {

    val ariaId = "name-menu-${state.ariaId}"

    IconButton {
      if (state.open) ariaOwns = ariaId
      ariaHasPopup = AriaHasPopup.`true`
      onClick = { event ->
        val target = event.currentTarget
        setState {
          open = !open
          anchorEl = target
        }
        event.preventDefault()
        event.stopPropagation()
      }
      ariaLabel = "More"
      MoreVert {
        if (props.config.fontSize != null) {
          asDynamic().fontSize = props.config.fontSize!!
        }
      }
    }
    Menu {
      onClose = { event: Event ->
        setState {
          open = false
          anchorEl = null
        }
        event.preventDefault()
        event.stopPropagation()
      }
      id = ariaId
      open = state.open
      asDynamic().anchorEl = state.anchorEl
      props.config.menuItems.forEach { item ->
        MenuItem {
          item.icon?.let { icon ->
            ListItemIcon {
              sx {
                minWidth = 36.px
              }
              icon {}
            }
          }
          sx {
            fontSize = 14.px
          }
          disabled = !item.enabled
          onClick = {
            item.onClick()
            setState { open = false }
          }
          +item.text
        }
      }
    }
  }
}

fun ChildrenBuilder.materialMenu(handler: MaterialMenuProps.() -> Unit) {
  MaterialMenu::class.react {
    +jso(handler)
  }
}

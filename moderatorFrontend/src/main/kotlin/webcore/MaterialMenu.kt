package webcore

import csstype.px
import mui.icons.material.MoreVert
import mui.icons.material.SvgIconComponent
import mui.material.*
import mui.system.sx
import org.w3c.dom.Element
import org.w3c.dom.events.Event
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
  var fontSize: SvgIconSize? = null,
  var menuItems: List<MenuItem>,
)

external interface MaterialMenuProps : Props {
  var config: MaterialMenuConfig
}

external interface MaterialMenuState : State {
  var open: Boolean
  var anchorEl: Element?
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
          fontSize = props.config.fontSize!!
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
      state.anchorEl?.let { anchorElement ->
        anchorEl = { anchorElement }
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
            setState { open = false }
          }
          +item.text
        }
      }
    }
  }
}

fun ChildrenBuilder.materialMenu(config: MaterialMenuConfig) {
  MaterialMenu::class.react {
    this.config = config
  }
}

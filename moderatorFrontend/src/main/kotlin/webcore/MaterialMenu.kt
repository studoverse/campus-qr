package webcore

import kotlinext.js.js
import org.w3c.dom.events.EventTarget
import react.*
import webcore.extensions.randomNumberString
import webcore.materialUI.*
import kotlin.random.Random

class MenuItem(
  val text: String,
  val enabled: Boolean = true,
  val icon: RClass<IconProps>? = null,
  val onClick: () -> Unit
)

external interface MaterialMenuProps : RProps {
  var classes: dynamic
  var className: String
  var fontSize: String?
  var menuItems: List<MenuItem>
}

external interface MaterialMenuState : RState {
  var open: Boolean
  var anchorEl: EventTarget?
  var ariaId: String
}

class MaterialMenu : RComponent<MaterialMenuProps, MaterialMenuState>() {

  override fun MaterialMenuState.init() {
    open = false
    anchorEl = null
    ariaId = Random.randomNumberString()
  }

  override fun RBuilder.render() {

    val ariaId = "name-menu-${state.ariaId}"

    iconButton {
      attrs.asDynamic()["aria-owns"] = if (state.open) ariaId else undefined
      attrs.asDynamic()["aria-haspopup"] = true
      attrs.onClick = { event ->
        val target = event.currentTarget
        setState {
          open = !open
          anchorEl = target
        }
        event.preventDefault()
        event.stopPropagation()
      }
      attrs.asDynamic()["aria-label"] = "More"
      moreVertIcon {
        if (props.fontSize != null) {
          attrs.fontSize = props.fontSize!!
        }
      }
    }
    menu {
      attrs.onClose = { event ->
        setState {
          open = false
          anchorEl = null
        }
        event.preventDefault()
        event.stopPropagation()
      }
      attrs.asDynamic()["id"] = ariaId
      attrs.open = state.open
      attrs.anchorEl = state.anchorEl
      props.menuItems.forEach { item ->
        menuItem {
          item.icon?.let { icon ->
            listItemIcon {
              attrs.classes = js {
                root = props.classes.itemIcon
              }
              icon {}
            }
          }
          attrs.classes = js {
            root = props.classes.menuItemStyle
          }
          attrs.disabled = !item.enabled
          attrs.onClick = {
            item.onClick()
            setState { open = false }
          }
          +item.text
        }
      }
    }
  }
}

private val style = { _: dynamic ->
  js {
    menuItemStyle = js {
      fontSize = 14
    }
    itemIcon = js {
      minWidth = 36
    }
  }
}

private val styledChatDrawer = withStyles<MaterialMenuProps, MaterialMenu>(style)

fun RBuilder.materialMenu(fontSize: String? = null, className: String = "", menuItems: List<MenuItem>) =
  styledChatDrawer {
    attrs.menuItems = menuItems
    attrs.className = className
    attrs.fontSize = fontSize
  }
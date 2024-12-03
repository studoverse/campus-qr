package webcore.materialMenu

import react.useState
import web.dom.Element
import webcore.ButtonOnClick
import webcore.Launch
import webcore.MenuOnClose

data class MaterialMenuController(
  val open: Boolean,
  val anchorEl: Element?,
  val onOpenMenuClick: ButtonOnClick,
  val onMenuClose: MenuOnClose,
  val onItemClick: (item: MaterialMenuConfig.Companion.MenuItem) -> Unit,
) {
  companion object {
    fun use(launch: Launch): MaterialMenuController {
      var open: Boolean by useState(false)
      var anchorEl: Element? by useState(null)

      val onOpenMenuClick: ButtonOnClick = { event ->
        val target = event.currentTarget
        open = !open
        anchorEl = target

        event.preventDefault()
        event.stopPropagation()
      }

      fun onItemClick(item: MaterialMenuConfig.Companion.MenuItem) {
        item.onClick()
        open = false
      }

      val onMenuClose: MenuOnClose = { event ->
        open = false
        anchorEl = null

        event.preventDefault()
        event.stopPropagation()
      }

      return MaterialMenuController(
        open = open,
        anchorEl = anchorEl,
        onOpenMenuClick = onOpenMenuClick,
        onItemClick = ::onItemClick,
        onMenuClose = onMenuClose,
      )
    }
  }
}
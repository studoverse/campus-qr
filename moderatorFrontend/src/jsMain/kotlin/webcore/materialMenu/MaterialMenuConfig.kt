package webcore.materialMenu

import mui.icons.material.SvgIconComponent
import mui.material.SvgIconSize

class MaterialMenuConfig(
  var className: String = "",
  var fontSize: SvgIconSize? = null,
  var menuItems: List<MenuItem>,
) {
  companion object {
    class MenuItem(
      val text: String,
      val enabled: Boolean = true,
      val icon: SvgIconComponent? = null,
      val onClick: () -> Unit,
    )
  }
}
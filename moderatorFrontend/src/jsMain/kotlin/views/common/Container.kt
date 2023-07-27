package views.common

import web.cssom.*
import mui.material.Box
import mui.system.sx
import react.ChildrenBuilder

fun ChildrenBuilder.container(block: ChildrenBuilder.() -> Unit): Unit =
  Box {
    className = ClassName("genericContainer")
    block(this)
  }

fun ChildrenBuilder.spacer(size: Int = 8) = Box {
  sx {
    height = size.px
  }
}

fun ChildrenBuilder.horizontalSpacer(size: Int = 8) = Box {
  sx {
    width = size.px
  }
}
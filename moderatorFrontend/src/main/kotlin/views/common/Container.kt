package views.common

import csstype.ClassName
import csstype.px
import kotlinx.js.jso
import mui.material.Box
import react.ChildrenBuilder

fun ChildrenBuilder.container(block: ChildrenBuilder.() -> Unit): Unit =
  Box {
    className = ClassName("genericContainer")
    block(this)
  }

fun ChildrenBuilder.spacer(size: Int = 8) = Box {
  sx = jso {
    height = size.px
  }
}

fun ChildrenBuilder.horizontalSpacer(size: Int = 8) = Box {
  sx = jso {
    width = size.px
  }
}
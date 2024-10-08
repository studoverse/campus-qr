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

fun ChildrenBuilder.spacer(size: Int = 8, key: String = "spacer1") = Box {
  this.key = key // Use static key to prevent component from being created from scratch after every render.
  sx {
    height = size.px
  }
}

fun ChildrenBuilder.horizontalSpacer(size: Int = 8, key: String = "horizontalSpacer1") = Box {
  this.key = key // Use static key to prevent component from being created from scratch after every render.
  sx {
    width = size.px
  }
}
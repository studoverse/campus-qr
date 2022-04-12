@file:Suppress("unused")

package webcore

import csstype.*
import mui.material.Box
import mui.material.Grid
import mui.material.GridDirection
import mui.system.ResponsiveStyleValue
import mui.system.sx
import react.ChildrenBuilder
import react.dom.html.ReactHTML.span

fun ChildrenBuilder.gridContainer(
  direction: GridDirection,
  rowGravity: JustifyContent = JustifyContent.start, // used when direction == ROW
  columnGravity: AlignContent = AlignContent.start, // used when direction == COLUMN
  spacing: Int = 1,
  alignItems: AlignItems = AlignItems.baseline,
  containerStyle: String? = null,
  items: ChildrenBuilder.() -> Unit
) {
  Grid {
    sx {
      this.alignItems = alignItems
      alignContent = columnGravity
      justifyContent = rowGravity
    }
    containerStyle?.let { className = ClassName(it) }
    container = true
    item = false
    this.direction = ResponsiveStyleValue(direction)
    this.spacing = ResponsiveStyleValue(spacing)
    items()
  }
}


/**
 * Must be called inside a gridContainer
 * @param size configure how much columns this item will take from its parent container
 * @param item used to override how item is positioned within the container or to align item's children
 * example style: {
 *     alignSelf = "center" || "stretch" || "baseline" etc. // align grid item inside the grid container
 *     textAlign = "center" || "start" etc.                 // align items within the grid items
 * }
 */
fun ChildrenBuilder.gridItem(
  size: GridSize = GridSize(xs = false),
  className: String? = null,
  item: ChildrenBuilder.() -> Unit
) {
  Grid {
    className?.let { this.className = ClassName(it) }
    this.item = true
    size.xs?.let { xs = it }
    size.sm?.let { sm = it }
    size.md?.let { md = it }
    size.lg?.let { lg = it }
    size.xl?.let { xl = it }
    item()
  }
}

/**
 * Configure grid container/item sizes for each screen size
 * values:
 * 1 - 12 number of columns will be taken
 * true : item will have equal size as other items in the row
 * auto : items equitably share the available space
 * false : item will not expand
 */
data class GridSize(
  val xs: Any? = "auto",
  val sm: Any? = null,
  val md: Any? = null,
  val lg: Any? = null,
  val xl: Any? = null
)

fun ChildrenBuilder.horizontalMargin(margin: Int) {
  Box {
    component = span
    sx {
      width = margin.px
      display = Display.inlineBlock
    }
  }
}

fun ChildrenBuilder.verticalMargin(margin: Int = 8) {
  Box {
    sx {
      height = margin.px
      display = Display.block
    }
  }
}

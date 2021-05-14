package webcore

import react.RBuilder
import react.dom.div
import react.dom.jsStyle
import react.dom.span
import webcore.materialUI.grid

fun RBuilder.gridContainer(
    direction: GridDirection,
    rowGravity: ItemsGravity = ItemsGravity.START, // used when direction == ROW
    columnGravity: ItemsGravity = ItemsGravity.START, // used when direction == COLUMN
    spacing: Int = 1,
    alignItems: String = "baseline",
    containerStyle: String? = null,
    items: RBuilder.() -> Unit) {
  grid {
    attrs.className = containerStyle
    attrs.container = true
    attrs.item = false
    attrs.alignContent = columnGravity.value
    attrs.justify = rowGravity.value
    attrs.direction = direction.value
    attrs.alignItems = alignItems
    attrs.spacing = spacing
    items()
  }
}


/**
 * must called inside a gridContainer
 * @param size configure how much columns this item will take from its parent container
 * @param itemStyle used to override how item is positioned within the container or to align item's children
 * example style: {
 *     alignSelf = "center" || "stretch" || "baseline" etc. // align grid item inside the grid container
 *     textAlign = "center" || "start" etc.                 // align items within the grid items
 * }
 */
fun RBuilder.gridItem(
    size: GridSize = GridSize(xs = false),
    className: String? = null,
    item: RBuilder.() -> Unit
) {
  grid {
    attrs.className = className
    attrs.item = true
    size.xs?.let { attrs.xs = it }
    size.sm?.let { attrs.sm = it }
    size.md?.let { attrs.md = it }
    size.lg?.let { attrs.lg = it }
    size.xl?.let { attrs.xl = it }
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

enum class GridDirection(val value: String) {
  ROW("row"),
  REVERSED_ROW("row-reverse"),
  COLUMN("column")
}

/**
 * ItemsGravity is representation of justify-content and align-content css property
 * only values which make sense is currently added.
 * feel free to add more values if needed :)
 */
enum class ItemsGravity(val value: String) {
  END("flex-end"),
  START("flex-start"),
  CENTER("center"),
  STRETCH("stretch"),
  SPACE_BETWEEN("space-between")
}


fun RBuilder.horizontalMargin(margin: Int) {
  span {
    attrs.jsStyle {
      marginRight = margin
    }
  }
}

fun RBuilder.verticalMargin(margin: Int) {
  div {
    attrs.jsStyle {
      marginBottom = margin
    }
  }
}

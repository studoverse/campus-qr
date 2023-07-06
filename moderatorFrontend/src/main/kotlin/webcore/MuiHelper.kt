package webcore

import csstype.PropertiesBuilder
import web.cssom.*
import js.core.jso
import mui.material.FormControlVariant
import mui.material.GridProps
import mui.material.InputBaseComponentProps
import mui.system.Union
import web.html.HTMLElement
import web.html.HTMLInputElement
import web.html.HTMLTextAreaElement
import react.*

val HTMLElement.value: String
  get() = when (this) {
    is HTMLInputElement -> value
    is HTMLTextAreaElement -> value
    else -> throw IllegalArgumentException("There is no value in this HTMLElement")
  }

var InputBaseComponentProps.min: Any? // String (e.g. "123", "2022-01-01'T'13:14:00") or Number (e.g. 123)
  get() = asDynamic().min
  set(value) {
    asDynamic().min = value
  }

var InputBaseComponentProps.max: Any? // String (e.g. "123", "2022-01-01'T'13:14:00") or Number (e.g. 123)
  get() = asDynamic().max
  set(value) {
    asDynamic().max = value
  }

var InputBaseComponentProps.list: String?
  get() = asDynamic().list
  set(value) {
    asDynamic().list = value
  }

var InputBaseComponentProps.maxLength: Int?
  get() = asDynamic().maxLength
  set(value) {
    asDynamic().maxLength = value
  }

var InputBaseComponentProps.pattern: String?
  get() = asDynamic().pattern
  set(value) {
    asDynamic().pattern = value
  }

inline var GridProps.xs: Any?
  get() = asDynamic().xs
  set(value) {
    asDynamic().xs = value
  }

inline var GridProps.sm: Any?
  get() = asDynamic().sm
  set(value) {
    asDynamic().sm = value
  }

inline var GridProps.md: Any?
  get() = asDynamic().md
  set(value) {
    asDynamic().md = value
  }

inline var GridProps.lg: Any?
  get() = asDynamic().lg
  set(value) {
    asDynamic().lg = value
  }

inline var GridProps.xl: Any?
  get() = asDynamic().xl
  set(value) {
    asDynamic().xl = value
  }

operator fun FormControlVariant.invoke(): Union = when (this) {
  FormControlVariant.outlined -> "outlined"
  FormControlVariant.filled -> "filled"
  FormControlVariant.standard -> "standard"
  else -> throw IllegalArgumentException("There is no variant with this name")
}

fun String.toReactNode() = ReactNode(source = this)

fun buildElements(handler: ChildrenBuilder.() -> Unit) {
  createElement(Fragment, jso {}, Fragment.create {
    handler()
  })
}

/** Select nested class */
fun PropertiesBuilder.nested(className: ClassName, style: PropertiesBuilder.() -> Unit) {
  (Selector("&.${className}")) {
    style()
  }
}

/** Used to set the marks of a Mui Slider component */
external interface SliderMark {
  var value: Int
  var label: String?
}

class OverflowForDialog(
  val overflowY: Overflow,
  val overflowX: Overflow,
)

/**
 * Due to issues with the `Portal` it is deactivated which leads to overflowing content being cut off at the edges of a Dialog/Popover etc.
 * To get the same behaviour as with a working `Portal` set overflow-y to `visible`
 * Note that overflowX needs to be set as well because if overflowY is `visible` and overflowX is `hidden`, `scroll` or `auto`
 * overflowY will be set to `auto` implicitly
 */
fun overflowForDialog(overflowX: Overflow = Overflow.visible): OverflowForDialog = OverflowForDialog(
  overflowY = Overflow.visible,
  overflowX = overflowX,
)

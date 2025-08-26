package webcore

import csstype.PropertiesBuilder
import js.objects.unsafeJso
import mui.base.AutocompleteChangeDetails
import mui.base.AutocompleteChangeReason
import mui.base.AutocompleteInputChangeReason
import mui.material.GridProps
import mui.material.InputBaseComponentProps
import mui.material.InputProps
import mui.material.TextFieldProps
import react.*
import react.dom.events.ChangeEvent
import react.dom.events.MouseEvent
import react.dom.events.SyntheticEvent
import web.cssom.*
import web.events.Event
import web.html.HTMLButtonElement
import web.html.HTMLElement
import web.html.HTMLInputElement
import web.html.HTMLTextAreaElement

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

fun gridArea(value: String): GridArea = value.unsafeCast<GridArea>()

// Use custom onChange here for TextFields, to specify that the ChangeEventHandler is of type HTMLInputElement
var TextFieldProps.onChange: react.dom.events.ChangeEventHandler<HTMLInputElement>?
  get() {
    return asDynamic().onChange as? react.dom.events.ChangeEventHandler<HTMLInputElement>?
  }
  set(value) {
    asDynamic().onChange = value
  }

inline var TextFieldProps.InputProps: InputProps
  get() = asDynamic().InputProps
  set(value) {
    asDynamic().InputProps = value
  }

fun String.toReactNode() = ReactNode(source = this)

fun buildElements(handler: ChildrenBuilder.() -> Unit) {
  createElement(Fragment, unsafeJso {}, Fragment.create {
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
 * Note that overflowX needs to be set as well because if overflowY is `visible` (default) and overflowX is `hidden`, `scroll` or `auto`
 * overflowY will be set to `auto` implicitly.
 * See: https://developer.mozilla.org/en-US/docs/Web/CSS/overflow-y
 */
fun overflowForDialog(overflowX: Overflow = Overflow.visible): OverflowForDialog = OverflowForDialog(
  overflowY = Overflow.visible,
  overflowX = overflowX,
)

/** Helper types to make it easier when defining members of a controller data class for functional components. */
typealias TextFieldOnChange = (ChangeEvent<HTMLInputElement>) -> Unit
typealias AutocompleteOnChange<T> = (SyntheticEvent<*, *>, Any, AutocompleteChangeReason, AutocompleteChangeDetails<T>?) -> Unit
typealias AutocompleteOnInputChange = (event: SyntheticEvent<*, *>, value: String, reason: AutocompleteInputChangeReason) -> Unit
typealias ButtonOnClick = (MouseEvent<HTMLButtonElement, *>) -> Unit
typealias SelectOnChange = (ChangeEvent<HTMLInputElement>, ReactNode) -> Unit
typealias MenuOnClose = (Event) -> Unit

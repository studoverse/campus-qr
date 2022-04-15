package webcore

import csstype.Flex
import csstype.FlexGrow
import mui.material.FormControlVariant
import mui.material.InputBaseComponentProps
import mui.system.Union
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import react.ReactNode

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

/** flex-grow: [grow], flex-shrink: 1, flex-basis: 0px */
fun Flex(
  grow: FlexGrow
): Flex =
  "$grow".unsafeCast<Flex>()

fun String.toReactNode() = ReactNode(source = this)

operator fun FormControlVariant.invoke(): Union = when (this) {
  FormControlVariant.outlined -> "outlined"
  FormControlVariant.filled -> "filled"
  FormControlVariant.standard -> "standard"
  else -> throw IllegalArgumentException("There is no variant with this name")
}

object TypographyVariant {
  val h1 = "h1"
  val h2 = "h2"
  val h3 = "h3"
  val h4 = "h4"
  val h5 = "h5"
  val h6 = "h6"
  val subtitle1 = "subtitle1"
  val subtitle2 = "subtitle2"
  val body1 = "body1"
  val body2 = "body2"
  val caption = "caption"
  val button = "button"
  val overline = "overline"
  val srOnly = "srOnly"
  val inherit = "inherit"
}

package webcore

import csstype.Flex
import csstype.FlexGrow
import csstype.Length
import csstype.Padding
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

fun Padding(
  allSides: Length,
): Padding =
  "$allSides".unsafeCast<Padding>()

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

enum class TypographyVariant(val value: String) {
  H1("h1"),
  H2("h2"),
  H3("h3"),
  H4("h4"),
  H5("h5"),
  H6("h6"),
  SUBTITLE1("subtitle1"),
  SUBTITLE2("subtitle2"),
  BODY1("body1"),
  BODY2("body2"),
  CAPTION("caption"),
  BUTTON("button"),
  OVERLINE("overline"),
  SR_ONLY("srOnly"),
  INHERIT("inherit")
}

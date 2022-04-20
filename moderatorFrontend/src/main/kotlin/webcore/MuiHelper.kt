package webcore

import csstype.*
import kotlinx.js.jso
import mui.material.FormControlVariant
import mui.material.InputBaseComponentProps
import mui.system.Union
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
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


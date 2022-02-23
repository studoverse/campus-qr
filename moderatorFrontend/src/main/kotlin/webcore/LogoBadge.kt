package webcore

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.img
import react.dom.jsStyle
import webcore.materialUI.typography

external interface LogoBadgeProps : RProps {
  var logoUrl: String
  var logoAlt: String
  var badgeTitle: String
  var badgeSubtitle: String?
}

class LogoBadge(props: LogoBadgeProps) : RComponent<LogoBadgeProps, RState>(props) {

  override fun RBuilder.render() {
    div {
      attrs.jsStyle {
        display = "flex"
        flexShrink = "0"
        flexDirection = "column"
        justifyContent = "center"
        alignItems = "center"
        padding = "15px 0"
        backgroundColor = "#f3f3f3"
      }

      img(src = props.logoUrl, alt = props.logoAlt) {
        attrs.jsStyle {
          width = 48
          height = 48
          margin = "8px 0"
        }
      }
      div {
        attrs.jsStyle {
          textAlign = "center"
        }
        typography {
          attrs.variant = "h5"
          +props.badgeTitle
        }
        props.badgeSubtitle?.let { badgeSubtitle ->
          div {
            attrs.jsStyle {
              margin = "0 8px"
              wordBreak = "break-word"
            }
            typography {
              attrs.variant = "subtitle1"
              +badgeSubtitle
            }
          }
        }
      }
    }
  }

}

fun RBuilder.logoBadge(logoUrl: String, logoAlt: String, badgeTitle: String, badgeSubtitle: String? = null) =
  child(LogoBadge::class) {
    attrs.badgeTitle = badgeTitle
    attrs.logoUrl = logoUrl
    attrs.logoAlt = logoAlt
    attrs.badgeSubtitle = badgeSubtitle
  }

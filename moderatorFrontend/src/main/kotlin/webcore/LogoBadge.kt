package webcore

import csstype.*
import mui.material.Box
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import org.w3c.dom.HTMLImageElement
import react.ChildrenBuilder
import react.Props
import react.State
import react.dom.html.ImgHTMLAttributes
import react.dom.html.ReactHTML.img
import react.react

class LogoBadgeConfig(
  var logoUrl: String,
  var logoAlt: String,
  var badgeTitle: String,
  var badgeSubtitle: String? = null,
)

external interface LogoBadgeProps : Props {
  var config: LogoBadgeConfig
}

private class LogoBadge(props: LogoBadgeProps) : RComponent<LogoBadgeProps, State>(props) {

  override fun ChildrenBuilder.render() {
    Box {
      sx {
        display = Display.flex
        flexShrink = number(0.0)
        flexDirection = FlexDirection.column
        justifyContent = JustifyContent.center
        alignItems = AlignItems.center
        padding = Padding(vertical = 15.px, horizontal = 0.px)
        backgroundColor = Color("#f3f3f3")
      }

      Box {
        component = img
        sx {
          width = 48.px
          height = 48.px
          margin = Margin(vertical = 8.px, horizontal = 0.px)
        }
        @Suppress("UNCHECKED_CAST", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
        this as ImgHTMLAttributes<HTMLImageElement>
        src = props.config.logoUrl
        alt = props.config.logoAlt
      }

      Box {
        sx {
          textAlign = TextAlign.center
        }
        Typography {
          variant = TypographyVariant.h5
          +props.config.badgeTitle
        }
        props.config.badgeSubtitle?.let { badgeSubtitle ->
          Box {
            sx {
              margin = Margin(vertical = 0.px, horizontal = 8.px)
              wordBreak = WordBreak.breakWord
            }
            Typography {
              variant = TypographyVariant.subtitle1
              +badgeSubtitle
            }
          }
        }
      }
    }
  }
}

fun ChildrenBuilder.logoBadge(config: LogoBadgeConfig) {
  LogoBadge::class.react {
    this.config = config
  }
}

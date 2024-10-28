package views.common

import js.lazy.Lazy
import mui.material.Box
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.Props
import react.dom.html.ReactHTML.code
import react.dom.html.ReactHTML.span
import web.cssom.Auto
import web.location.location
import webcore.FcWithCoroutineScope

external interface PathNotFoundProps : Props {}

@Lazy
val PathNotFoundFc = FcWithCoroutineScope<PathNotFoundProps> { props, launch ->
  Box {
    sx {
      margin = Auto.auto
    }
    Typography {
      sx {
        centeredText()
      }
      variant = TypographyVariant.h1
      +"404"
    }
    Typography {
      sx {
        centeredText()
      }
      variant = TypographyVariant.body1
      +"Path \""
      span {
        code {
          +location.pathname
        }
      }
      +"\" doesn't seem to exist. Try something else."
    }
  }
}

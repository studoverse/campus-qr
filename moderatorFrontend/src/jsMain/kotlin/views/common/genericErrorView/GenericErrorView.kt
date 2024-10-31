package views.common.genericErrorView

import web.cssom.*
import mui.material.Box
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.Props
import js.lazy.Lazy
import views.common.centeredText
import webcore.FcWithCoroutineScope

external interface GenericErrorViewProps : Props {
  var config: GenericErrorViewConfig
}

@Lazy
val GenericErrorView = FcWithCoroutineScope<GenericErrorViewProps> { props, launch ->
  Box {
    sx {
      margin = Auto.auto
    }
    Typography {
      sx {
        centeredText()
      }
      variant = TypographyVariant.h5
      +props.config.title
    }
    Typography {
      sx {
        centeredText()
      }
      variant = TypographyVariant.body1
      +props.config.subtitle
    }
  }
}

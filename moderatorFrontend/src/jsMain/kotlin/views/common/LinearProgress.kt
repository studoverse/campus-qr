package views.common

import web.cssom.*
import mui.material.Box
import mui.material.LinearProgress
import mui.system.sx
import react.Props
import webcore.FcWithCoroutineScope

external interface MbLinearProgressProps : Props {
  var show: Boolean
}

/**
 * Linear progress that has a fixed height of 8px also when it's not shown.
 */
val MbLinearProgressFc = FcWithCoroutineScope<MbLinearProgressProps> { props, launch ->
  Box {
    sx {
      height = 8.px
    }
    if (props.show) {
      LinearProgress {}
    }
  }
}

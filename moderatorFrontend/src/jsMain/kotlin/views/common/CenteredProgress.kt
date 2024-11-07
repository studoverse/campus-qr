package views.common

import web.cssom.*
import mui.material.Box
import mui.material.CircularProgress
import mui.system.sx
import react.Props
import webcore.FcWithCoroutineScope

external interface CenteredProgressProps : Props

val CenteredProgress = FcWithCoroutineScope<CenteredProgressProps> { props, launch ->
  Box {
    sx {
      display = Display.flex
      alignSelf = AlignSelf.center
      marginTop = Auto.auto
      marginBottom = Auto.auto
      justifyContent = JustifyContent.center
    }
    CircularProgress {}
  }
}

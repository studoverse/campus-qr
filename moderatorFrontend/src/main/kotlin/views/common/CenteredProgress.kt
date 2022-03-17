package views.common

import csstype.AlignSelf
import csstype.Auto
import csstype.Display
import csstype.JustifyContent
import kotlinx.js.jso
import mui.material.Box
import mui.material.CircularProgress
import react.ChildrenBuilder
import react.Props
import react.State
import react.react
import webcore.RComponent

external interface CenteredProgressProps : Props

external interface CenteredProgressState : State

class CenteredProgress : RComponent<CenteredProgressProps, CenteredProgressState>() {
  override fun ChildrenBuilder.render() {
    Box {
      sx = jso {
        display = Display.flex
        alignSelf = AlignSelf.center
        marginTop = Auto.auto
        marginBottom = Auto.auto
        justifyContent = JustifyContent.center
      }
      CircularProgress {}
    }
  }
}

fun ChildrenBuilder.centeredProgress(handler: CenteredProgressProps.() -> Unit = {}) {
  CenteredProgress::class.react {
    +jso(handler)
  }
}
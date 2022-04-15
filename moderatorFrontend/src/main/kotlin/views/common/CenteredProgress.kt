package views.common

import csstype.AlignSelf
import csstype.Auto
import csstype.Display
import csstype.JustifyContent
import mui.material.Box
import mui.material.CircularProgress
import mui.system.sx
import react.ChildrenBuilder
import react.Props
import react.State
import react.react
import webcore.RComponent

external interface CenteredProgressProps : Props

external interface CenteredProgressState : State

private class CenteredProgress : RComponent<CenteredProgressProps, CenteredProgressState>() {
  override fun ChildrenBuilder.render() {
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
}

fun ChildrenBuilder.centeredProgress() {
  CenteredProgress::class.react {}
}
package views.common

import web.cssom.*
import mui.material.Box
import mui.material.LinearProgress
import mui.system.sx
import react.ChildrenBuilder
import react.Props
import react.State
import react.react
import webcore.RComponent

external interface MbLinearProgressProps : Props {
  var show: Boolean
}

external interface MbLinearProgressState : State

/**
 * Linear progress that has a fixed height of 8px also when it's not shown.
 */
private class MbLinearProgress : RComponent<MbLinearProgressProps, MbLinearProgressState>() {

  override fun ChildrenBuilder.render() {
    Box {
      sx {
        height = 8.px
      }
      if (props.show) {
        LinearProgress {}
      }
    }
  }
}

fun ChildrenBuilder.renderMbLinearProgress(show: Boolean) {
  MbLinearProgress::class.react {
    this.show = show
  }
}

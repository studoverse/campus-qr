package views.common

import csstype.px
import kotlinx.js.Object.Companion.create
import kotlinx.js.jso
import mui.material.Box
import mui.material.LinearProgress
import react.*

external interface MbLinearProgressProps : Props {
  var show: Boolean
}

external interface MbLinearProgressState : State

/**
 * Linear progress that has a fixed height of 8px also when it's not shown.
 */
class MbLinearProgress : Component<MbLinearProgressProps, MbLinearProgressState>() {
  override fun render(): ReactNode {
    return Box.create {
      sx = jso {
        height = 8.px
      }
      if (props.show) {
        LinearProgress {}
      }
    }
  }
}

/*fun renderMbLinearProgress(show: Boolean) = MbLinearProgress<MbLinearProgressProps, MbLinearProgressState>.create {
  this.show = show
}*/

// TODO: @mh Find out how to render a class component
fun ChildrenBuilder.renderMbLinearProgress(handler: MbLinearProgressProps.() -> Unit) {
  create(MbLinearProgress::class, handler)
  //child(MbLinearProgress::class, handler)
}

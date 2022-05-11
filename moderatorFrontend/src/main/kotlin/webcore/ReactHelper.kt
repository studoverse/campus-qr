package webcore

import kotlinext.js.assign
import kotlinx.js.jso
import react.*

abstract class RComponent<P : Props, S : State> : Component<P, S> {
  constructor() : super() {
    state = jso { init() }
  }

  constructor(props: P) : super(props) {
    state = jso { init(props) }
  }

  open fun S.init() {}

  // if you use this method, don't forget to pass props to the constructor first
  open fun S.init(props: P) {}

  abstract fun ChildrenBuilder.render()

  override fun render(): ReactNode = Fragment.create { render() }
}

fun <S : State> Component<*, S>.setState(buildState: S.() -> Unit) {
  setState({ assign(it, buildState) })
}

/**
 * Convenience function for defining customContent in DialogConfig
 * Can only be used for trivial cases where no extra props, state handling or callbacks are required other than a basic ChildrenBuilder block
 * WILL NOT WORK FOR STATE UPDATES IN THE CALL SITE!
 */
fun basicCustomContent(block: ChildrenBuilder.() -> Unit) =
  DialogConfig.CustomContent(component = BasicCustomContentDialog::class, setProps = {
    config = BasicCustomContentDialogConfig(block = {
      block()
    })
  })

class BasicCustomContentDialogConfig(
  val block: ChildrenBuilder.() -> Unit
)

external interface BasicCustomContentDialogProps : Props {
  var config: BasicCustomContentDialogConfig
}

@Suppress("UPPER_BOUND_VIOLATED")
class BasicCustomContentDialog(props: BasicCustomContentDialogProps) :
  RComponentWithCoroutineScope<BasicCustomContentDialogProps, State>(props) {

  override fun ChildrenBuilder.render() {
    props.config.block(this)
  }
}

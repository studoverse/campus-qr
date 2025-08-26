package webcore

import js.objects.unsafeJso
import kotlinx.coroutines.Job
import react.*

abstract class RComponent<P : Props, S : State> : Component<P, S> {
  constructor() : super() {
    state = unsafeJso { init() }
  }

  constructor(props: P) : super(props) {
    state = unsafeJso { init(props) }
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

typealias Launch = (suspend () -> Unit) -> Job
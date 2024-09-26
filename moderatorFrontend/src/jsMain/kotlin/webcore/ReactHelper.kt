package webcore

import js.objects.jso
import kotlinext.js.assign
import react.*

abstract class RComponent<P : Props, S : State> : Component<P, S> {
  constructor() : super() {
    // TODO: @mh This is not called anymore. Maybe some stuff with the underlying imported Component class changed?
    console.log("RComponent constructor") // TODO: @mh Remove after testing
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
  setState({ assign(it, buildState) }) // TODO: @mh Test that this still works
}

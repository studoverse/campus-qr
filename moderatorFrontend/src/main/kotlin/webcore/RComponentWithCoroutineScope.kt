package webcore

import kotlinx.coroutines.*
import react.*

abstract class RComponentWithCoroutineScope<P : Props, S : State> : RComponent<P, S> {
  val componentScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

  constructor() : super()

  constructor(props: P) : super(props)

  /**
   * Launch a coroutine in this component's scope. Its Job will be canceled when component unmounts.
   * Only call after component has been mounted, e.g. in componentDidMount().
   */
  fun launch(block: suspend () -> Unit) = componentScope.launch { block.invoke() }

  override fun componentWillUnmount() {
    if (componentScope.isActive) {
      componentScope.cancel("Cancel coroutine because component will unmount")
    }
  }
}

/**
 * Provide coroutine scope within functional components to allow cancellations
 */
fun <P : Props> functionalComponentWithCoroutineScope(
  func: ChildrenBuilder.(props: P, componentScope: CoroutineScope) -> Unit,
): FC<P> = FC { props ->
  val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
  buildElements {
    func(props, scope)
  }
}


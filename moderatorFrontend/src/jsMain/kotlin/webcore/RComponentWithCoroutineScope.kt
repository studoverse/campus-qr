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
@Suppress("FunctionName") fun <P : Props> FcWithCoroutineScope(
  block: ChildrenBuilder.(props: P, componentScope: CoroutineScope) -> Unit,
) = FC<P> { props ->
  val scope = useMemo(*emptyArray()) { CoroutineScope(Dispatchers.Default + SupervisorJob()) }

  useEffectOnceWithCleanup {
    onCleanup {
      // TODO: @mh Check if this is called when the component is unmounted or if unmounting works differently.
      console.log("onUnmount") // TODO: @mh Remove after testing

      if (scope.isActive) {
        scope.cancel("Cancel coroutine because component will unmount")
      }
    }
  }

  block(props, scope)
}


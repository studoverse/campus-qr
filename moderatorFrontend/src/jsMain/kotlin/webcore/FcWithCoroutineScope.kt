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
 * Provide coroutine scope within functional components to allow cancellations.
 */
@Suppress("FunctionName") fun <P : Props> FcWithCoroutineScope(
  displayName: String? = null,
  block: ChildrenBuilder.(props: P, launch: Launch) -> Unit,
) = FC<P>(displayName ?: "No displayName defined") { props ->
  val scope = useMemo(*emptyArray<Any>()) { CoroutineScope(Dispatchers.Default + SupervisorJob()) }

  // TODO for future: @mh Use context parameters for this once released: https://github.com/Kotlin/KEEP/issues/367
  fun launch(block: suspend () -> Unit) = scope.launch {
    try {
      block.invoke()
    } catch (e: Exception) {
      console.error("Coroutine failed", e)  // Log the actual exception
    }
  }

  useEffectOnceWithCleanup {
    onCleanup {
      if (scope.isActive) {
        scope.cancel("Cancel coroutine because component will unmount")
      }
    }
  }

  block(props, ::launch)
}

@Suppress("FunctionName") fun <P : PropsWithRef<*>> FcRefWithCoroutineScope(
  block: ChildrenBuilder.(props: P, launch: Launch) -> Unit,
) = FC<P> { props ->
  val scope = useMemo(*emptyArray<Any>()) { CoroutineScope(Dispatchers.Default + SupervisorJob()) }

  // TODO for future: @mh Use context parameters for this once released: https://github.com/Kotlin/KEEP/issues/367
  fun launch(block: suspend () -> Unit) = scope.launch {
    try {
      block.invoke()
    } catch (e: Exception) {
      console.error("Coroutine failed", e)  // Log the actual exception
    }
  }

  useEffectOnceWithCleanup {
    onCleanup {
      if (scope.isActive) {
        scope.cancel("Cancel coroutine because component will unmount")
      }
    }
  }

  block(props, ::launch)
}


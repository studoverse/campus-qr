package webcore.extensions

import kotlin.coroutines.*
import kotlin.js.Promise


suspend fun <T> Promise<T>.await() = suspendCoroutine<T> { continuation ->
  then { value -> continuation.resume(value) }

  catch { exception ->
    @Suppress("USELESS_ELVIS") // Necessary since reject can be called without parameter
    continuation.resumeWithException(exception ?: Exception("Empty promise rejection"))
  }
}

suspend fun <T> Promise<T>.awaitOrNull(): T? = suspendCoroutine { continuation ->
  then { continuation.resume(value = it) }
  catch { continuation.resume(null) }
}

fun <T> async(block: suspend () -> T): Promise<T> = Promise { resolve, reject ->
  block.startCoroutine(object : Continuation<T> {
    override fun resumeWith(result: Result<T>) {
      if (result.isSuccess) {
        resolve(result.getOrThrow())
      } else {
        reject(result.exceptionOrNull()!!)
      }
    }

    override val context: CoroutineContext get() = EmptyCoroutineContext
  })
}

fun launch(block: suspend () -> Unit) {
  async(block).catch { exception -> console.error("Failed with $exception") }
}
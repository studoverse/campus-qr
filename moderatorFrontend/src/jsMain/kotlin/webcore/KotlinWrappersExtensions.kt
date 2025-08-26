package webcore

import js.objects.Object
import js.objects.unsafeJso
import kotlin.apply
import kotlin.collections.forEach
import kotlin.js.RegExp
import kotlin.js.asDynamic

/** Copied from deprecated kotlin-extensions of kotlin-wrappers. */

fun <T : Any> clone(obj: T) = Object.Companion.assign(unsafeJso(), obj)
inline fun <T : Any> assign(obj: T, builder: T.() -> Unit) = clone(obj).apply(builder)

fun <T> requireAll(context: Context<T>) = context.keys().forEach(context::invoke)

external object require {
  fun resolve(module: String): String

  // Note: require.context is a webpack-specific function
  fun <T> context(
    directory: String,
    useSubdirectories: Boolean = definedExternally,
    regExp: RegExp = definedExternally,
    mode: String = definedExternally
  ): Context<T>
}

external fun <T> require(module: String): T

external interface Context<T> : JsFunction1<String, T> {
  fun resolve(module: String): String
  fun keys(): Array<String>
  val id: Int
}

external interface JsFunction<in C, out O> {
  fun call(ctx: C, vararg args: Any?): O
  fun apply(ctx: C, args: Array<out Any?>): O
  fun bind(ctx: C, vararg args: Any?): JsFunction<Nothing?, O>

  val length: Int
}

external interface JsFunction0<out O> : JsFunction<Nothing?, O>

operator fun <O> JsFunction0<O>.invoke() = asDynamic()()

external interface JsFunction1<in I, out O> : JsFunction<Nothing?, O>

operator fun <I, O> JsFunction1<I, O>.invoke(arg: I) =
  asDynamic()(arg)

external interface JsFunction2<in I1, in I2, out O> : JsFunction<Nothing?, O>

operator fun <I1, I2, O> JsFunction2<I1, I2, O>.invoke(arg1: I1, arg2: I2) =
  asDynamic()(arg1, arg2)

external interface JsFunction3<in I1, in I2, in I3, out O> : JsFunction<Nothing?, O>

operator fun <I1, I2, I3, O> JsFunction3<I1, I2, I3, O>.invoke(arg1: I1, arg2: I2, arg3: I3) =
  asDynamic()(arg1, arg2, arg3)
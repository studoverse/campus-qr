package util

import com.studo.campusqr.common.utils.LocalizedString

abstract class MbUrl(
  val path: String,
  val title: LocalizedString,
  val requiresAuth: Boolean = true,
  val showWithShell: Boolean = true,
) {
  val name get() = this::class.simpleName ?: "Anonymous"

  init {
    if (urls.any { it.path == this.path }) {
      throw IllegalStateException("Duplicate path at ${this.name} is not allowed by design. We need a 1:1 mapping of AppRoutes and paths")
    }

    @Suppress("LeakingThis")
    urls += this
  }

  companion object {
    val urls: MutableSet<MbUrl> = mutableSetOf()
  }
}
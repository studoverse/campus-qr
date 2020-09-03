package views.login

import kotlinx.browser.window
import org.w3c.dom.url.URL
import react.RComponent
import util.toRoute

abstract class LoginDetailComponent<P : react.RProps, S : react.RState> : RComponent<P, S>() {
  protected fun redirectAfterLogin() {
    // Fallback admin page if no redirect url was defined, reload the page
    val redirectUrl = window.location.toRoute()?.queryParams?.get("redirect") ?: "/admin/"

    // Use baseUrl of current origin to safely get current hostname and construct href from relative url
    // https://developer.mozilla.org/en-US/docs/Web/API/URL
    val parsedRedirectUrl = URL(/* url = */redirectUrl, /* baseUrl = */window.location.origin)
        .takeIf { it.hostname == window.location.hostname }

    window.location.href = parsedRedirectUrl?.href ?: "/admin/" // Fall back if parsedRedirectUrl was invalid
  }
}
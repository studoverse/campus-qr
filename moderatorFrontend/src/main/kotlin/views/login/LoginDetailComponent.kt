package views.login

import web.location.location
import web.url.URL
import webcore.RComponent
import webcore.extensions.toRoute

abstract class LoginDetailComponent<P : react.Props, S : react.State> : RComponent<P, S>() {
  protected fun redirectAfterLogin() {
    // Fallback admin page if no redirect url was defined, reload the page
    val redirectUrl = location.toRoute()?.queryParams?.get("redirect") ?: "/admin/"

    // Use baseUrl of current origin to safely get current hostname and construct href from relative url
    // https://developer.mozilla.org/en-US/docs/Web/API/URL
    val parsedRedirectUrl = URL(/* url = */redirectUrl, /* baseUrl = */location.origin)
      .takeIf { it.hostname == location.hostname }

    location.href = parsedRedirectUrl?.href ?: "/admin/" // Fall back if parsedRedirectUrl was invalid
  }
}
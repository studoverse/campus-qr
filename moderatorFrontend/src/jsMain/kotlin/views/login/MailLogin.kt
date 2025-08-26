package views.login

import com.studo.campusqr.common.LoginResult
import com.studo.campusqr.common.payloads.MailLoginData
import csstype.PropertiesBuilder
import js.lazy.Lazy
import web.cssom.*
import kotlinx.browser.document
import mui.material.Box
import mui.material.FormControlVariant
import mui.material.TextField
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.Props
import react.Suspense
import react.dom.events.ChangeEvent
import react.dom.html.ReactHTML.form
import react.useState
import util.Strings
import util.apiBase
import util.get
import views.common.spacer
import views.login.loginNavigationButtonsViewFc.LoginNavigationButtonsViewConfig
import views.login.loginNavigationButtonsViewFc.LoginNavigationButtonsView
import web.html.HTMLElement
import web.html.InputType
import web.html.password
import web.location.location
import web.url.URL
import webcore.*
import webcore.extensions.launch
import webcore.extensions.toRoute

external interface MailLoginProps : Props

private fun PropertiesBuilder.description() {
  textAlign = TextAlign.center
  padding = 16.px
}

@Lazy
val MailLogin = FcWithCoroutineScope<MailLoginProps> { props, componentScope ->
  var email: String by useState("")
  var password: String by useState("")
  var networkRequestInProgress: Boolean by useState(false)
  var errorMessage: String? by useState(null)

  fun redirectAfterLogin() {
    // Fallback admin page if no redirect url was defined, reload the page
    val redirectUrl = location.toRoute()?.queryParams?.get("redirect") ?: "/admin/"

    // Use baseUrl of current origin to safely get current hostname and construct href from relative url
    // https://developer.mozilla.org/en-US/docs/Web/API/URL
    val parsedRedirectUrl = URL(/* url = */redirectUrl, /* baseUrl = */location.origin)
      .takeIf { it.hostname == location.hostname }

    location.href = parsedRedirectUrl?.href ?: "/admin/" // Fall back if parsedRedirectUrl was invalid
  }

  fun login() {
    networkRequestInProgress = true

    launch {
      val response: LoginResult? = NetworkManager.post<String>(
        url = "$apiBase/user/login",
        body = MailLoginData(
          email = email,
          password = password
        ),
        headers = mapOf(
          "csrfToken" to document.querySelector("meta[name='csrfToken']")!!.getAttribute("content")!!
        )
      )?.let { result -> LoginResult.values().find { it.name == result } ?: LoginResult.UNKNOWN_ERROR }

      networkRequestInProgress = false
      errorMessage = when (response) {
        LoginResult.LOGIN_FAILED -> Strings.login_wrong_email_and_pw.get()
        LoginResult.LOGIN_SUCCESSFUL -> {
          launch {
            redirectAfterLogin()
          }
          null
        }

        LoginResult.UNKNOWN_ERROR -> Strings.login_unknown_error.get()
        null -> Strings.network_error_description.get()
      }
    }
  }

  Typography {
    sx {
      description()
    }
    variant = TypographyVariant.body1
    +Strings.login_email_form_body.get()
  }

  form {
    onSubmit = { event ->
      event.preventDefault()
      event.stopPropagation()
      login()
    }
    Box {
      sx {
        padding = 16.px
      }
      TextField {
        autoComplete = "email"
        fullWidth = true
        label = Strings.email_address.get().toReactNode()
        onChange = { event: ChangeEvent<HTMLElement> ->
          email = event.target.value
          errorMessage = null

        }
        variant = FormControlVariant.standard
        value = email
      }
      spacer(key = "spacer1")
      TextField {
        autoComplete = "current-password"
        fullWidth = true
        type = InputType.password
        label = Strings.login_email_form_pw_label.get().toReactNode()
        onChange = { event ->
          val value = event.target.value
          password = value
          errorMessage = null
        }
        variant = FormControlVariant.standard
        value = password
      }
      errorMessage?.let { errorMessage ->
        Typography {
          variant = TypographyVariant.body1
          sx {
            description()
          }
          +errorMessage
        }
      }
      spacer(size = 32, key = "spacer2")
      Suspense {
        LoginNavigationButtonsView {
          config = LoginNavigationButtonsViewConfig(
            networkRequestInProgress = networkRequestInProgress,
            backEnabled = false,
            nextButtonText = Strings.login_login_button.get(),
            nextButtonDisabled = email.isEmpty() || password.isEmpty(),
            onNextAction = {
              login()
            }
          )
        }
      }
    }
  }
  Box {
    sx {
      description()
    }
    Typography {
      +Strings.login_forgot_pw_text.get()
    }
  }
}

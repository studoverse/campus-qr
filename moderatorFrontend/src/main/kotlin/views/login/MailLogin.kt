package views.login

import com.studo.campusqr.common.LoginResult
import com.studo.campusqr.common.payloads.MailLoginData
import csstype.PropertiesBuilder
import csstype.TextAlign
import csstype.px
import kotlinx.browser.document
import kotlinx.js.jso
import mui.material.*
import mui.system.sx
import org.w3c.dom.HTMLElement
import react.*
import react.dom.events.ChangeEvent
import react.dom.html.InputType
import react.dom.html.ReactHTML.form
import util.Strings
import util.apiBase
import util.get
import views.common.spacer
import webcore.NetworkManager
import webcore.extensions.launch
import webcore.invoke
import webcore.setState
import webcore.value

external interface MailLoginProps : Props

external interface MailLoginState : State {
  var email: String
  var password: String
  var networkRequestInProgress: Boolean
  var errorMessage: String?
}

@Suppress("UPPER_BOUND_VIOLATED")
private class MailLogin : LoginDetailComponent<MailLoginProps, MailLoginState>() {
  override fun MailLoginState.init() {
    email = ""
    password = ""
    networkRequestInProgress = false
    errorMessage = null
  }

  private fun login() {
    setState { networkRequestInProgress = true }
    launch {
      val response: LoginResult? = NetworkManager.post<String>(
        url = "$apiBase/user/login",
        body = MailLoginData(
          email = state.email,
          password = state.password
        ),
        headers = mapOf(
          "csrfToken" to document.querySelector("meta[name='csrfToken']")!!.getAttribute("content")!!
        )
      )?.let { result -> LoginResult.values().find { it.name == result } ?: LoginResult.UNKNOWN_ERROR }

      setState {
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
  }

  override fun ChildrenBuilder.render() {
    Typography {
      sx {
        description()
      }
      variant = "body1"
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
        TextField<StandardTextFieldProps> {
          fullWidth = true
          label = ReactNode(Strings.email_address.get())
          onChange = { event: ChangeEvent<HTMLElement> ->
            setState {
              email = event.target.value
              errorMessage = null
            }
          }
          variant = FormControlVariant.standard()
          value = state.email
        }
        spacer()
        TextField<StandardTextFieldProps> {
          fullWidth = true
          type = InputType.password
          label = ReactNode(Strings.login_email_form_pw_label.get())
          onChange = { event: dynamic ->
            val value = event.target.value
            setState {
              password = value
              errorMessage = null
            }
          }
          variant = FormControlVariant.standard()
          value = state.password
        }
        state.errorMessage?.let { errorMessage ->
          Typography {
            variant = "body1"
            sx {
              description()
            }
            +errorMessage
          }
        }
        spacer(32)
        renderLoginNavigationButtonsView {
          config = LoginNavigationButtonsViewConfig(
            networkRequestInProgress = state.networkRequestInProgress,
            backEnabled = false,
            nextButtonText = Strings.login_login_button.get(),
            nextButtonDisabled = state.email.isEmpty() || state.password.isEmpty(),
            onNextAction = {
              login()
            }
          )
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

  private fun PropertiesBuilder.description() {
    textAlign = TextAlign.center
    padding = 16.px
  }
}

fun ChildrenBuilder.renderMailLogin(handler: MailLoginProps.() -> Unit = {}) {
  MailLogin::class.react {
    +jso(handler)
  }
}

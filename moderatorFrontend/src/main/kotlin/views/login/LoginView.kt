package views.login

import app.ColorPalette
import app.baseUrl
import com.studo.campusqr.common.payloads.UserData
import com.studo.campusqr.common.payloads.isAuthenticated
import kotlinext.js.js
import kotlinx.browser.document
import kotlinx.browser.window
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.a
import react.dom.div
import react.dom.img
import util.Strings
import util.get
import views.login.LoginMode.EMAIL
import webcore.materialUI.*

interface LoginViewProps : RProps {
  var classes: LoginViewClasses
  var loginMode: LoginMode
  var userData: UserData
}

enum class LoginMode {
  EMAIL
}

interface LoginViewState : RState

class LoginView : RComponent<LoginViewProps, LoginViewState>() {

  override fun RBuilder.render() {
    document.body?.style?.backgroundColor = "#f2f2f2"

    muiCard {
      attrs.className = props.classes.cardOuter
      muiCardHeader {
        attrs.classes = js {
          root = props.classes.cardHeader
        }
        attrs.title = div {
          img(classes = props.classes.logoImg) {
            attrs.src = "$baseUrl/static/images/logo_campusqr.png"
            attrs.alt = "Logo"
          }
        }
      }
      muiCardContent {
        div {
          when (props.loginMode) {
            EMAIL -> renderMailLogin()
          }
        }
      }
    }
    typography {
      attrs.className = props.classes.companyInfo
      attrs.variant = "body2"

      a(href = baseUrl) {
        +Strings.login_info.get()
      }
    }
  }
}

interface LoginViewClasses {
  var cardOuter: String
  var cardHeader: String
  var logoImg: String
  var companyInfo: String
}

private val style = { _: dynamic ->
  js {
    cardOuter = js {
      maxWidth = 450
      marginTop = 36
      marginLeft = "auto"
      marginRight = "auto"
      marginBottom = 16
    }
    cardHeader = js {
      padding = 0
      background = "white"
    }
    logoImg = js {
      display = "block"
      marginLeft = "auto"
      marginRight = "auto"
      marginTop = 36
      height = "auto"
      width = 100
    }
    companyInfo = js {
      textAlign = "center"
      color = ColorPalette.gray
    }
  }
}

private val styled = withStyles<LoginViewProps, LoginView>(style)

fun RBuilder.renderLoginView(studoUserData: UserData, mode: LoginMode) = styled {
  // Set component attrs here
  attrs.loginMode = mode
  attrs.userData = studoUserData

  if (studoUserData.isAuthenticated) {
    // User is authenticated so redirect to main page
    window.location.href = "/admin"
  }
}
  

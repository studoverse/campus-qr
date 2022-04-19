package views.login

import app.AppContext
import app.ColorPalette
import app.appContext
import app.baseUrl
import com.studo.campusqr.common.payloads.isAuthenticated
import csstype.*
import kotlinx.browser.document
import kotlinx.browser.window
import mui.material.*
import mui.system.sx
import org.w3c.dom.HTMLImageElement
import react.*
import react.dom.html.ImgHTMLAttributes
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import util.Strings
import util.get
import views.login.LoginMode.EMAIL
import webcore.RComponent
import webcore.TypographyVariant

external interface LoginViewProps : Props {
  var loginMode: LoginMode
}

enum class LoginMode {
  EMAIL
}

external interface LoginViewState : State

private class LoginView : RComponent<LoginViewProps, LoginViewState>() {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(LoginView::class) {
    init {
      this.contextType = appContext
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  override fun componentDidMount() {
    if (appContext.userDataContext.userData!!.isAuthenticated) {
      // User is authenticated so redirect to main page
      window.location.href = "/admin"
    }
  }

  override fun ChildrenBuilder.render() {
    document.body?.style?.backgroundColor = "#f2f2f2"

    Card {
      sx {
        maxWidth = 450.px
        marginTop = 36.px
        marginLeft = Auto.auto
        marginRight = Auto.auto
        marginBottom = 16.px
      }
      CardHeader {
        sx {
          padding = 0.px
          background = NamedColor.white
        }
        title = div.create {
          Box {
            component = img
            sx {
              display = Display.block
              marginLeft = Auto.auto
              marginRight = Auto.auto
              marginTop = 36.px
              height = Auto.auto
              width = 100.px
            }
            this as ImgHTMLAttributes<HTMLImageElement>
            src = "$baseUrl/static/images/logo_campusqr.png"
            alt = "Logo"
          }
        }
      }
      CardContent {
        div {
          when (props.loginMode) {
            EMAIL -> renderMailLogin()
          }
        }
      }
    }
    Typography {
      sx {
        textAlign = TextAlign.center
        color = Color(ColorPalette.gray)
      }
      variant = TypographyVariant.body2

      a {
        href = baseUrl
        +Strings.login_info.get()
      }
    }
  }
}

fun ChildrenBuilder.renderLoginView(loginMode: LoginMode) {
  LoginView::class.react {
    this.loginMode = loginMode
  }
}
  

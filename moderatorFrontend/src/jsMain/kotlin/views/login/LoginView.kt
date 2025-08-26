package views.login

import web.cssom.*
import app.ColorPalette
import app.appContextToInject
import app.baseUrl
import com.studo.campusqr.common.payloads.isAuthenticated
import js.lazy.Lazy
import kotlinx.browser.document
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import web.html.HTMLImageElement
import react.*
import react.dom.html.ImgHTMLAttributes
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import util.Strings
import util.get
import views.login.LoginViewConfig.Companion.LoginMode
import web.location.location
import webcore.FcWithCoroutineScope

external interface LoginViewProps : Props {
  var config: LoginViewConfig
}

@Lazy
val LoginView = FcWithCoroutineScope<LoginViewProps> { props, launch ->
  val appContext = use(appContextToInject)!!
  useEffectOnce {
    if (appContext.userDataContext.userData!!.isAuthenticated) {
      // User is authenticated so redirect to main page
      location.href = "/admin"
    }
  }

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
          @Suppress("UNCHECKED_CAST", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
          this as ImgHTMLAttributes<HTMLImageElement>
          src = "$baseUrl/static/images/logo_campusqr.png"
          alt = "Logo"
        }
      }
    }
    CardContent {
      Box {
        when (props.config.loginMode) {
          LoginMode.EMAIL -> Suspense { MailLogin {} }
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

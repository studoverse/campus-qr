package views.login

import web.cssom.*
import app.ColorPalette
import app.appContextToInject
import app.baseUrl
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
import views.login.LoginMode.EMAIL
import webcore.FcWithCoroutineScope

external interface LoginViewProps : Props {
  var loginMode: LoginMode
}

enum class LoginMode {
  EMAIL
}

val LoginViewFc = FcWithCoroutineScope { props: LoginViewProps, componentScope ->
  //val appContext = useContext(appContextToInject)
  useEffectOnceWithCleanup { // TODO: @mh Maybe emptyArray can be replaced with listOf() ?
    console.log("onMount")

    // TODO: @mh How to access appContext?
    /*if (appContext!!.userDataContext.userData!!.isAuthenticated) {
      // User is authenticated so redirect to main page
      location.href = "/admin"
    }*/

    onCleanup {
      // TODO: @mh Check if this is called when the component is unmounted or if unmounting works differently.
      console.log("onUnmount")
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
        when (props.loginMode) {
          EMAIL -> MailLoginFc {}
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

package views.adminInfo

import csstype.Globals
import csstype.PropertiesBuilder
import csstype.px
import kotlinx.browser.window
import kotlinx.js.jso
import mui.material.Box
import mui.material.Button
import mui.material.ButtonColor
import mui.material.Typography
import mui.system.sx
import react.ChildrenBuilder
import react.Props
import react.State
import react.dom.html.ReactHTML.br
import react.react
import util.Strings
import util.get
import views.common.spacer
import webcore.RComponent

external interface AdminInfoProps : Props

external interface AdminInfoState : State

class AdminInfo : RComponent<AdminInfoProps, AdminInfoState>() {
  override fun ChildrenBuilder.render() {
    Box {
      sx {
        marginTop = 8.px
        marginLeft = 16.px
        marginRight = marginLeft
        marginBottom = 32.px
      }
      Typography {
        sx {
          marginTop = 8.px
          marginBottom = 16.px
        }
        variant = "h5"
        +Strings.admin_info.get()
      }
      Typography {
        sx {
          subheader()
        }
        variant = "h6"
        +Strings.user_sso_info.get()
      }
      +Strings.user_sso_info_details1.get()
      br {}
      +Strings.user_sso_info_details2.get()
      Typography {
        sx {
          subheader()
        }
        variant = "h6"
        +Strings.admin_info_configuration.get()
      }
      +Strings.admin_info_configuration_details.get()
      spacer(16)
      Box {
        Button {
          sx {
            textTransform = Globals.initial
            marginLeft = (-8).px
          }
          color = ButtonColor.primary
          onClick = {
            window.open("https://studo.com", "_blank")
          }
          +Strings.more_about_studo.get()
        }
      }
    }
  }

  fun PropertiesBuilder.subheader() {
    marginTop = 8.px
    marginBottom = 8.px
  }
}

fun ChildrenBuilder.renderAdminInfo(handler: AdminInfoProps.() -> Unit) {
  AdminInfo::class.react {
    +jso(handler)
  }
}
  
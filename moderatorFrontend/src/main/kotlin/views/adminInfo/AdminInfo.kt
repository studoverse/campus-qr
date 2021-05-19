package views.adminInfo

import kotlinext.js.js
import kotlinx.browser.window
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.br
import react.dom.div
import util.Strings
import util.get
import views.common.spacer
import webcore.materialUI.muiButton
import webcore.materialUI.typography
import webcore.materialUI.withStyles

interface AdminInfoProps : RProps {
  var classes: AdminInfoClasses
}

interface AdminInfoState : RState

class AdminInfo : RComponent<AdminInfoProps, AdminInfoState>() {
  override fun RBuilder.render() {
    div(classes = props.classes.container) {
      typography {
        attrs.className = props.classes.header
        attrs.variant = "h5"
        +Strings.admin_info.get()
      }
      typography {
        attrs.className = props.classes.subheader
        attrs.variant = "h6"
        +Strings.user_sso_info.get()
      }
      +Strings.user_sso_info_details1.get()
      br {}
      +Strings.user_sso_info_details2.get()
      typography {
        attrs.className = props.classes.subheader
        attrs.variant = "h6"
        +Strings.admin_info_configuration.get()
      }
      +Strings.admin_info_configuration_details.get()
      spacer(16)
      div {
        muiButton {
          attrs.className = props.classes.contactButton
          attrs.color = "primary"
          attrs.onClick = {
            window.open("https://studo.com", "_blank")
          }
          +Strings.more_about_studo.get()
        }
      }
    }
  }
}

interface AdminInfoClasses {
  var header: String
  var subheader: String
  var contactButton: String
  var container: String
}

private val style = { _: dynamic ->
  js {
    header = js {
      marginTop = 8
      marginBottom = 16
    }
    subheader = js {
      marginTop = 8
      marginBottom = 8
    }
    contactButton = js {
      textTransform = "initial"
      marginLeft = -8
    }
    container = js {
      marginTop = 8
      marginLeft = 16
      marginRight = marginLeft
      marginBottom = 32
    }
  }
}

private val styled = withStyles<AdminInfoProps, AdminInfo>(style)

fun RBuilder.renderAdminInfo() = styled {
  // Set component attrs here
}
  
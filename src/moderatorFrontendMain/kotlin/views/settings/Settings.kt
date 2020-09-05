package views.settings

import com.studo.campusqr.common.UserData
import kotlinext.js.js
import kotlinx.browser.window
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.br
import util.Strings
import util.get
import webcore.materialUI.muiButton
import webcore.materialUI.withStyles

interface SettingsProps : RProps {
  var classes: SettingsClasses
  var userData: UserData?
}

interface SettingsState : RState

class Settings : RComponent<SettingsProps, SettingsState>() {
  override fun RBuilder.render() {
    if (props.userData?.externalAuthProvider == false) {
      muiButton {
        attrs.className = props.classes.button
        attrs.color = "primary"
        attrs.onClick = {
          window.location.href = "/accountSettings"
        }
        +Strings.account_settings.get()
      }
      br {}
    }
    renderLanguageSwitch()
    br {}
    muiButton {
      attrs.className = props.classes.button
      attrs.color = "primary"
      attrs.onClick = {
        window.location.href = "/user/logout"
      }
      +Strings.logout.get()
    }
  }
}

interface SettingsClasses {
  var button: String
}

private val SettingsStyle = { theme: dynamic ->
  js {
    button = js {
      textTransform = "initial"
    }
  }
}

private val styled = withStyles<SettingsProps, Settings>(SettingsStyle)

fun RBuilder.renderSettings(userData: UserData?) = styled {
  // Set component attrs here
  attrs.userData = userData
}
  
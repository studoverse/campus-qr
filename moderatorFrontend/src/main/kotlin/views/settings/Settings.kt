package views.settings

import app.baseUrl
import com.studo.campusqr.common.UserData
import kotlinext.js.js
import kotlinx.browser.window
import pathBase
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
}

interface SettingsState : RState

class Settings : RComponent<SettingsProps, SettingsState>() {
  override fun RBuilder.render() {
    renderLanguageSwitch()
    br {}
    muiButton {
      attrs.className = props.classes.button
      attrs.color = "primary"
      attrs.onClick = {
        window.location.href = "$baseUrl/user/logout"
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

fun RBuilder.renderSettings() = styled {
  // Set component attrs here
}
  
package views.settings

import app.baseUrl
import kotlinext.js.js
import kotlinx.browser.window
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.br
import util.Strings
import util.get
import webcore.materialUI.muiButton
import webcore.materialUI.withStyles

external interface SettingsProps : Props {
  var classes: SettingsClasses
}

external interface SettingsState : State

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

external interface SettingsClasses {
  var button: String
}

private val style = { _: dynamic ->
  js {
    button = js {
      textTransform = "initial"
    }
  }
}

private val styled = withStyles<SettingsProps, Settings>(style)

fun RBuilder.renderSettings() = styled {
  // Set component attrs here
}
  
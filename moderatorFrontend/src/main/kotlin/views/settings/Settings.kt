package views.settings

import app.baseUrl
import csstype.Globals
import kotlinx.browser.window
import kotlinx.js.jso
import mui.material.Button
import mui.material.ButtonColor
import mui.system.sx
import react.ChildrenBuilder
import react.Props
import react.State
import react.dom.html.ReactHTML.br
import react.react
import util.Strings
import util.get
import webcore.RComponent

external interface SettingsProps : Props

external interface SettingsState : State

class Settings : RComponent<SettingsProps, SettingsState>() {
  override fun ChildrenBuilder.render() {
    renderLanguageSwitch {}
    br {}
    Button {
      sx {
        textTransform = Globals.initial
      }
      color = ButtonColor.primary
      onClick = {
        window.location.href = "$baseUrl/user/logout"
      }
      +Strings.logout.get()
    }
  }
}

fun ChildrenBuilder.renderSettings(handler: SettingsProps.() -> Unit = {}) {
  Settings::class.react {
    +jso(handler)
  }
}
  
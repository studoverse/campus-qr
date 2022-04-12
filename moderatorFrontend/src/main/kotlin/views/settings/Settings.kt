package views.settings

import app.baseUrl
import csstype.Globals
import kotlinx.browser.window
import mui.material.Button
import mui.material.ButtonColor
import mui.system.sx
import react.ChildrenBuilder
import react.Props
import react.State
import react.react
import util.Strings
import util.get
import webcore.RComponent
import webcore.verticalMargin

external interface SettingsProps : Props

external interface SettingsState : State

private class Settings : RComponent<SettingsProps, SettingsState>() {
  override fun ChildrenBuilder.render() {
    renderLanguageSwitch()
    verticalMargin(16)
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

fun ChildrenBuilder.renderSettings() {
  Settings::class.react {}
}
  
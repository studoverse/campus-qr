package views.settings

import app.baseUrl
import csstype.Globals
import mui.material.Button
import mui.material.ButtonColor
import mui.system.sx
import react.ChildrenBuilder
import react.Props
import react.State
import react.react
import util.Strings
import util.get
import web.location.location
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
        location.href = "$baseUrl/user/logout"
      }
      +Strings.logout.get()
    }
  }
}

fun ChildrenBuilder.renderSettings() {
  Settings::class.react {}
}
  
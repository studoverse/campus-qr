package views.settings

import app.baseUrl
import web.cssom.*
import mui.material.Button
import mui.material.ButtonColor
import mui.system.sx
import react.Props
import util.Strings
import util.get
import web.location.location
import webcore.FcWithCoroutineScope
import webcore.verticalMargin

external interface SettingsProps : Props

val SettingsFc = FcWithCoroutineScope<SettingsProps> { props, launch ->
  LanguageSwitchFc {}
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

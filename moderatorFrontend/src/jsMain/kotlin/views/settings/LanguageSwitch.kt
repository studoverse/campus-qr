package views.settings

import app.appContextToInject
import web.cssom.*
import mui.material.Box
import mui.material.Switch
import mui.material.SwitchColor
import mui.system.sx
import react.*
import util.MbLocalizedStringConfig
import webcore.FcWithCoroutineScope

external interface LanguageSwitchProps : Props

val LanguageSwitch = FcWithCoroutineScope<LanguageSwitchProps> { props, launch ->
  val appContext = use(appContextToInject)!!
  val languageContext = appContext.languageContext

  val activeLanguage = languageContext.activeLanguage
  val languageChange = languageContext.onLanguageChange

  Box {
    sx {
      display = Display.flex
      justifyContent = JustifyContent.center
      alignItems = AlignItems.center
      fontFamily = string("'Roboto', Arial, sans-serif")
    }
    +"Deutsch"
    Switch {
      checked = (activeLanguage != MbLocalizedStringConfig.SupportedLanguage.De)
      color = SwitchColor.default
      onChange = { _, checked ->
        val newLanguage = if (checked) {
          MbLocalizedStringConfig.SupportedLanguage.En
        } else {
          MbLocalizedStringConfig.SupportedLanguage.De
        }
        languageChange(newLanguage)
      }
    }
    +"English"
  }
}

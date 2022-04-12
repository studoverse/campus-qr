package views.settings

import app.languageContext
import csstype.AlignItems
import csstype.Display
import csstype.JustifyContent
import csstype.string
import mui.material.Box
import mui.material.Switch
import mui.material.SwitchColor
import mui.system.sx
import react.*
import util.MbLocalizedStringConfig
import webcore.RComponent

external interface LanguageSwitchProps : Props

private class LanguageSwitch(props: LanguageSwitchProps) : RComponent<LanguageSwitchProps, State>(props) {
  override fun ChildrenBuilder.render() {

    languageContext.Consumer {
      children = { languageState ->
        val activeLanguage = languageState.activeLanguage
        val languageChange = languageState.onLanguageChange

        Box.create {
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
    }
  }
}

fun ChildrenBuilder.renderLanguageSwitch() {
  LanguageSwitch::class.react {}
}

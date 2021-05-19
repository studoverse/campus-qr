package views.settings

import app.languageContext
import kotlinext.js.js
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import util.MbLocalizedStringConfig
import webcore.materialUI.switch
import webcore.materialUI.withStyles

interface LanguageSwitchProps : RProps {
  var classes: LanguageSwitchClasses
}

private class LanguageSwitch(props: LanguageSwitchProps) : RComponent<LanguageSwitchProps, RState>(props) {
  override fun RBuilder.render() {

    languageContext.Consumer { languageState ->
      val activeLanguage = languageState.activeLanguage
      val languageChange = languageState.onLanguageChange

      div(classes = props.classes.root) {
        +"Deutsch"
        switch {
          attrs.checked = (activeLanguage != MbLocalizedStringConfig.SupportedLanguage.De)
          attrs.color = "default"
          attrs.onChange = { _, checked ->
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

interface LanguageSwitchClasses {
  var root: String
}

private val style = { _: dynamic ->
  js {
    root = js {
      display = "flex"
      justifyContent = "center"
      alignItems = "center"
      fontFamily = "'Roboto', Arial, sans-serif"
    }
  }
}

private val styled = withStyles<LanguageSwitchProps, LanguageSwitch>(style)

fun RBuilder.renderLanguageSwitch() = styled {}

package views.settings

import app.AppContext
import app.appContextToInject
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

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(LanguageSwitch::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  override fun ChildrenBuilder.render() {
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
}

fun ChildrenBuilder.renderLanguageSwitch() {
  LanguageSwitch::class.react {}
}

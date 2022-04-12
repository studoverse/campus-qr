package app

import mui.material.styles.Theme
import react.createContext
import util.AppRoute
import util.MbLocalizedStringConfig

data class LanguageContext(
  val activeLanguage: MbLocalizedStringConfig.SupportedLanguage,
  val onLanguageChange: (newLang: MbLocalizedStringConfig.SupportedLanguage) -> Unit
)

data class RouteContext(val pushRoute: (AppRoute) -> Unit)

data class ThemeContext(val theme: Theme)

data class AppContext(
  val languageContext: LanguageContext,
  val routeContext: RouteContext,
  val themeContext: ThemeContext,
) {
  // Shorthand to write `appContext.theme` instead of `appContext.themeContext.theme`
  val theme: Theme get() = themeContext.theme
}

val appContext = createContext<AppContext>()
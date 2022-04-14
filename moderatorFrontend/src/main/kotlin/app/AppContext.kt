package app

import com.studo.campusqr.common.payloads.UserData
import mui.material.styles.Theme
import react.createContext
import util.AppRoute
import util.MbLocalizedStringConfig

data class LanguageContext(
  val activeLanguage: MbLocalizedStringConfig.SupportedLanguage,
  val onLanguageChange: (newLang: MbLocalizedStringConfig.SupportedLanguage) -> Unit
)

data class RouteContext(
  val currentAppRoute: AppRoute?,
  val pushRoute: (AppRoute) -> Unit,
)

data class ThemeContext(val theme: Theme)

data class UserDataContext(
  val userData: UserData?,
  val loadingUserData: Boolean,
  val fetchNewUserData: () -> Unit,
)

data class AppContext(
  val languageContext: LanguageContext,
  val routeContext: RouteContext,
  val themeContext: ThemeContext,
  val userDataContext: UserDataContext,
) {
  // Shorthand to write `appContext.theme` instead of `appContext.themeContext.theme`
  val theme: Theme get() = themeContext.theme
}

val appContext = createContext<AppContext>()
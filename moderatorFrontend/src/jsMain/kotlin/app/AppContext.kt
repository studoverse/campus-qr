package app

import MbSnackbarConfig
import com.studo.campusqr.common.payloads.UserData
import mui.material.styles.Theme
import react.RefObject
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
  private val snackbarRef: RefObject<MbSnackbarRef>,
  val routeContext: RouteContext,
  val themeContext: ThemeContext,
  val userDataContext: UserDataContext,
) {
  // Shorthand to write `appContext.theme` instead of `appContext.themeContext.theme`
  val theme: Theme get() = themeContext.theme

  // Use showSnackbar(string) for simple snackbars that only show a simple information text.
  fun showSnackbarText(text: String) = snackbarRef.current!!.showSnackbarText(text)

  // Use showSnackbar(MbSnackbarConfig) for advanced snackbars
  fun showSnackbar(config: MbSnackbarConfig) = snackbarRef.current!!.showSnackbar(config)

  // The snackbar always closes automatically after [SnackbarProps.autoHideDuration].
  // [MbSnackbar.closeSnackbar] is only needed if you use [MbSnackbarConfig.action] or [MbSnackbarConfig.complexMessage] and
  // have a custom close action where the snackbar should be closed immediately.
  fun closeSnackbar() = snackbarRef.current!!.closeSnackbar()
}

val appContextToInject = createContext<AppContext>()
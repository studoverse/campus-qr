package app

import com.studo.campusqr.common.payloads.UserData
import mui.material.styles.Theme
import react.createContext
import util.AppRoute
import util.MbLocalizedStringConfig
import webcore.DialogConfig
import webcore.MbSnackbarConfig

data class LanguageContext(
  val activeLanguage: MbLocalizedStringConfig.SupportedLanguage,
  val onLanguageChange: (newLang: MbLocalizedStringConfig.SupportedLanguage) -> Unit
)

data class MbSnackbarContext(
  val showSnackbar: (String) -> Unit,
  val showSnackbarAdvanced: (MbSnackbarConfig) -> Unit
)

data class MbDialogContext(
  val showDialog: (DialogConfig) -> Unit,
  val closeDialog: () -> Unit,
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
  val snackbarContext: MbSnackbarContext,
  val dialogContext: MbDialogContext,
  val routeContext: RouteContext,
  val themeContext: ThemeContext,
  val userDataContext: UserDataContext,
) {
  // Shorthand to write `appContext.theme` instead of `appContext.themeContext.theme`
  val theme: Theme get() = themeContext.theme

  fun showDialog(dialogConfig: DialogConfig) = dialogContext.showDialog(dialogConfig)

  /**
   * Close dialog
   * Use only for buttons in customContent of DialogConfig()
   */
  fun closeDialog() = dialogContext.closeDialog()

  // Use showSnackbar(string) for simple snackbars that only show a simple information text.
  fun showSnackbar(text: String) = snackbarContext.showSnackbar(text)

  // Use showSnackbar(MbSnackbarConfig) for advanced snackbars
  fun showSnackbar(config: MbSnackbarConfig) = snackbarContext.showSnackbarAdvanced(config)
}

val appContextToInject = createContext<AppContext>()
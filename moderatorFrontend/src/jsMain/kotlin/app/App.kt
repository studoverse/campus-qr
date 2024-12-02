package app

import MbSnackbar
import csstype.*
import kotlinx.browser.document
import mui.material.Box
import mui.material.styles.ThemeProvider
import mui.system.sx
import react.*
import web.cssom.*
import web.location.location
import webcore.*
import webcore.shell.AppShellConfig
import webcore.shell.AppShell

val baseUrl = location.href.substringBefore("/admin")

external interface AppProps : Props

object ColorPalette {
  const val default = "#FFFFFF" // White
  const val primaryColor = "#5994F0"
  const val secondaryColor = "#FF4081"
  const val gray = "#9D9D9D"
  const val textDefault = "#000000"
}

object GlobalCss {
  const val flex = "flex"
  const val flexEnd = "flexEnd"
  const val fullWidth = "fullWidth"

  fun PropertiesBuilder.flex() {
    display = Display.flex
    flexDirection = FlexDirection.row
    flexWrap = FlexWrap.wrap
  }
}

val App = FcWithCoroutineScope<AppProps> { props, launch ->
  val controller = AppController.use(launch = launch)

  document.body?.style?.backgroundColor = "white"

  val fetchNewUserData = {
    controller.fetchUserDataAndInit(null)
  }

  ThemeProvider {
    this.theme = controller.theme
    appContextToInject.Provider(
      AppContext(
        languageContext = LanguageContext(controller.activeLanguage, controller.onLangChange),
        snackbarRef = controller.snackbarRef,
        routeContext = RouteContext(controller.currentAppRoute, controller.pushAppRoute),
        themeContext = ThemeContext(theme),
        userDataContext = UserDataContext(userData = controller.userData, controller.loadingUserData, fetchNewUserData)
      )
    ) {
      // Global components
      MbSnackbar { ref = controller.snackbarRef }
      MbDialog { ref = controller.navigationHandlerDialogRef }

      // Render content without side drawer and toolbar, if no shell option is activated via url hash
      if (location.hash.contains("noShell") ||
        controller.currentAppRoute?.url?.showWithShell != true
      ) {
        Box {
          sx {
            // Viewport height to take up whole screen because parent has no height set (similar as for appShell).
            // Especially important when we show only the iFrameView.
            minHeight = 100.vh
            width = 100.pct
            display = Display.flex
            flexDirection = FlexDirection.column
          }
          controller.renderViewContent(this)
        }
      } else {
        AppShell {
          config = AppShellConfig(
            appBarElevation = 0,
            mobileNavOpen = controller.mobileNavOpen,
            mobileNavOpenChange = controller.mobileNavOpenChange,
            smallToolbar = true,
            stickyNavigation = true,
            viewContent = {
              controller.renderViewContent(this)
            },
            drawerList = {
              AppDrawerItems {
                config = AppDrawerItemsConfig(
                  checkInSideDrawerItems = if (controller.loadingUserData) emptyList() else controller.checkInSideDrawerItems,
                  moderatorSideDrawerItems = if (controller.loadingUserData) emptyList() else controller.moderatorSideDrawerItems,
                  adminSideDrawerItems = if (controller.loadingUserData) emptyList() else controller.adminSideDrawerItems,
                  loading = false,
                  mobileNavOpenChange = controller.mobileNavOpenChange,
                )
              }
            },
            toolbarIcon = null,
            hideDrawer = false,
            themeColor = controller.theme.palette.primary.main,
          )
        }
      }
    }
  }
}

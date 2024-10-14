package app

import MbSnackbarFc
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
import webcore.shell.AppShellFc

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

val AppFc = FcWithCoroutineScope<AppProps> { props, launch ->
  val appController = AppController.useAppController(launch = launch)

  document.body?.style?.backgroundColor = "white"
  document.body?.classList?.add("testCss") // TODO: @mh Remove after testing

  val fetchNewUserData = {
    appController.fetchUserDataAndInit(null)
  }

  // TODO: @mh Migrate this to the new functional component style.
  ThemeProvider {
    this.theme = appController.theme
    appContextToInject.Provider(
      AppContext(
        languageContext = LanguageContext(appController.activeLanguage, appController.onLangChange),
        snackbarRef = appController.snackbarRef,
        // TODO: @mh Check if pushAppRoute works like that without ":".
        routeContext = RouteContext(appController.currentAppRoute, appController.pushAppRoute),
        themeContext = ThemeContext(theme),
        userDataContext = UserDataContext(userData = appController.userData, appController.loadingUserData, fetchNewUserData)
      )
    ) {
      // Global components
      MbSnackbarFc { ref = appController.snackbarRef }
      MbDialogFc { ref = appController.navigationHandlerDialogRef }

      // Render content without side drawer and toolbar, if no shell option is activated via url hash
      if (location.hash.contains("noShell") ||
        appController.currentAppRoute?.url?.showWithShell != true
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
          appController.renderViewContent(this)
        }
      } else {
        AppShellFc {
          config = AppShellConfig(
            appBarElevation = 0,
            mobileNavOpen = appController.mobileNavOpen,
            mobileNavOpenChange = appController.mobileNavOpenChange,
            smallToolbar = true,
            stickyNavigation = true,
            viewContent = {
              appController.renderViewContent(this)
            },
            drawerList = {
              AppDrawerItemsFc {
                config = AppDrawerItemsConfig(
                  checkInSideDrawerItems = if (appController.loadingUserData) emptyList() else appController.checkInSideDrawerItems(),
                  moderatorSideDrawerItems = if (appController.loadingUserData) emptyList() else appController.moderatorSideDrawerItems(),
                  adminSideDrawerItems = if (appController.loadingUserData) emptyList() else appController.adminSideDrawerItems(),
                  loading = false,
                  mobileNavOpenChange = appController.mobileNavOpenChange,
                )
              }
            },
            toolbarIcon = null,
            hideDrawer = false,
            themeColor = appController.theme.palette.primary.main,
          )
        }
      }
    }
  }
}

package app

import MbSnackbar
import com.studo.campusqr.common.UserPermission
import com.studo.campusqr.common.extensions.emptyToNull
import com.studo.campusqr.common.payloads.*
import csstype.*
import js.objects.jso
import kotlinx.browser.document
import kotlinx.coroutines.Job
import mui.icons.material.*
import mui.material.Box
import mui.material.styles.Theme
import mui.material.styles.ThemeProvider
import mui.material.styles.createTheme
import mui.system.sx
import react.*
import react.dom.flushSync
import util.*
import util.Url
import web.cssom.*
import web.location.location
import webcore.*
import webcore.NavigationHandler.allUrls
import webcore.extensions.toRoute
import webcore.shell.AppShellConfig
import webcore.shell.appShell

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

private fun isNotAuthenticatedButRequiresAuth(currentRoute: AppRoute?, currentUserData: UserData): Boolean {
  return !currentUserData.isAuthenticated && currentRoute?.url?.requiresAuth != false
}

private fun calculateRedirectQueryParams(): Map<String, String> = location.relativeUrl
  .removeSuffix("/")
  .takeIf { it != "/admin" && it != "/admin/login" } // Default path, no need to redirect
  ?.emptyToNull() // Do not redirect to empty url, it's safely handled in the router but the url would look strange
  ?.let { mapOf("redirect" to it) }
  ?: emptyMap()

val AppFc = FcWithCoroutineScope { props: AppProps, launch ->
  val appController = AppController.useAppController(launch = launch)

  document.body?.style?.backgroundColor = "white"
  console.log("render") // TODO: @mh Remove after testing

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
      //mbSnackbar(ref = snackbarRef) // TODO: @mh Migrate later
      //mbDialog(ref = navigationHandlerDialogRef) // TODO: @mh Migrate later

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
        appShell(
          config = AppShellConfig(
            appBarElevation = 0,
            mobileNavOpen = appController.mobileNavOpen,
            mobileNavOpenChange = { newMobileNavOpen ->
              appController.setMobileNavOpen(newMobileNavOpen)
            },
            smallToolbar = true,
            stickyNavigation = true,
            viewContent = {
              appController.renderViewContent(this)
            },
            drawerList = {
              renderAppDrawerItems(
                config = AppDrawerItemsConfig(
                  checkInSideDrawerItems = if (appController.loadingUserData) emptyList() else appController.checkInSideDrawerItems(),
                  moderatorSideDrawerItems = if (appController.loadingUserData) emptyList() else appController.moderatorSideDrawerItems(),
                  adminSideDrawerItems = if (appController.loadingUserData) emptyList() else appController.adminSideDrawerItems(),
                  loading = false,
                  mobileNavOpenChange = { newMobileNavOpen ->
                    appController.setMobileNavOpen(newMobileNavOpen)
                  }
                )
              )
            },
            toolbarIcon = null,
            hideDrawer = false,
            themeColor = appController.theme.palette.primary.main,
          )
        )
      }
    }
  }
}

// TODO: @mh Check if all of these functions are really needed outside of the controller.
private data class AppController(
  val userData: UserData?,
  val loadingUserData: Boolean,
  val currentAppRoute: AppRoute?,
  val mobileNavOpen: Boolean,
  val setMobileNavOpen: StateSetter<Boolean>,
  val activeLanguage: MbLocalizedStringConfig.SupportedLanguage,
  val theme: Theme,
  val snackbarRef: MutableRefObject<MbSnackbar>,
  val navigationHandlerDialogRef: MutableRefObject<MbDialog>,
  val checkInSideDrawerItems: () -> List<SideDrawerItem>,
  val moderatorSideDrawerItems: () -> List<SideDrawerItem>,
  val adminSideDrawerItems: () -> List<SideDrawerItem>,
  val pushAppRoute: (AppRoute) -> Unit,
  val handleHistoryChange: (newRoute: AppRoute?, currentUserData: UserData?) -> Unit,
  val onLangChange: (newLang: MbLocalizedStringConfig.SupportedLanguage) -> Unit,
  val fetchUserDataAndInit: (block: ((UserData) -> Unit)?) -> Unit,
  val renderViewContent: ChildrenBuilder.() -> Unit,
) {
  companion object {
    fun useAppController(launch: (suspend () -> Unit) -> Job): AppController {
      var userData: UserData? by useState(null)
      var loadingUserData: Boolean by useState(true)
      var currentAppRoute: AppRoute? by useState(null)
      var (mobileNavOpen: Boolean, setMobileNavOpen) = useState(false)
      var activeLanguage: MbLocalizedStringConfig.SupportedLanguage by useState(MbLocalizedStringConfig.selectedLanguage)

      // Only used for the NavigationHandler since this dialog must exist globally
      var navigationHandlerDialogRef = useRef<MbDialog>() // TODO: @mh Figure out how to use useRef correctly.
      var snackbarRef = useRef<MbSnackbar>()

      // TODO: @mh Check that this works
      lateinit var pushAppRoute: (AppRoute) -> Unit // Workaround for function not being visible for handleHistoryChange.

      fun handleHistoryChange(newRoute: AppRoute? = location.toRoute(), currentUserData: UserData? = userData) {
        if (newRoute == null) {
          console.log("Omit history change for 404 page")
        } else if (currentUserData == null) {
          // Do not update `currentAppRoute` when `userData` is not yet initialized.
          // `isNotAuthenticatedButRequiresAuth` will then be checked again after mounting.
          // `currentAppRoute` is set after mounting.
        } else if (isNotAuthenticatedButRequiresAuth(currentRoute = newRoute, currentUserData = currentUserData)) {
          // The user might end here by clicking back after logging out.
          // componentDidMount() won't be called as going back is not mounting a new component.
          // The user is not logged in so push him to login page
          pushAppRoute(Url.LOGIN_EMAIL.toRoute(queryParams = calculateRedirectQueryParams())!!)
        } else {
          console.info("Change route to ${newRoute.relativeUrl}")
          currentAppRoute = newRoute
        }
      }

      pushAppRoute = { route ->
        if (NavigationHandler.shouldNavigate(route, NavigationHandler.NavigationEvent.PUSH_APP_ROUTE, ::handleHistoryChange)) {
          NavigationHandler.pushHistory(
            relativeUrl = route.relativeUrl,
            title = route.url.title.get(),
            handleHistoryChange = ::handleHistoryChange,
          )
          NavigationHandler.resetScrollPosition()
        }
      }

      fun onLangChange(newLang: MbLocalizedStringConfig.SupportedLanguage) {
        activeLanguage = newLang
        MbLocalizedStringConfig.selectedLanguage = newLang
      }

      fun fetchUserDataAndInit(block: ((UserData) -> Unit)? = null) = launch {
        val fetchedUserData = NetworkManager.get<UserData>("$apiBase/user/data")

        if (fetchedUserData != null) {
          // userData needs to be set when calling `block`, so execute the state update beforehand
          // TODO: @mh Make sure to check all flushSync usages to prevent issues like this from happening.
          flushSync { // TODO: @mh Might not be needed anymore since I have to pass the new value manually anyway. And not related to UI.
            userData = fetchedUserData
            loadingUserData = false
          }
          block?.invoke(fetchedUserData)
        } else {
          loadingUserData = false
        }
      }

      fun ChildrenBuilder.renderViewContent() {
        if (loadingUserData || (location.toRoute() != null && currentAppRoute == null)) {
          // Wait for the network request in fetchUserDataAndInit() to complete or wait for currentAppRoute to be set if the route exists.
          // Path not found is handled in renderAppContent()
          // centeredProgress() // TODO: @mh Migrate later
        } else if (userData == null) {
          //networkErrorView() // TODO: @mh Migrate later
        } else {
          AppContentFc {}
        }
      }

      useEffectOnceWithCleanup { // TODO: @mh Maybe emptyArray can be replaced with listOf() ?
        console.log("onMount") // TODO: @mh Remove after testing

        NavigationHandler.initApp(
          allUrls = Url.entries,
          dialogRef = navigationHandlerDialogRef,
          handleHistoryChange = ::handleHistoryChange,
          getCurrentAppRoute = { currentAppRoute },
        )

        val duplicatePaths = allUrls.groupBy { it.path }.filter { it.value.count() > 1 }.keys
        if (duplicatePaths.isNotEmpty()) {
          throw IllegalStateException(
            "Duplicate path at ${duplicatePaths.first()} is not allowed by design. " +
                "We need a 1:1 mapping of AppRoutes and paths"
          )
        }
        fetchUserDataAndInit { updatedUserData ->
          // Do not use currentAppRoute here, because it's not set yet.
          // currentAppRoute will be set in this function through pushAppRoute/handleHistoryChange.
          val currentRoute = location.toRoute()

          when {
            isNotAuthenticatedButRequiresAuth(currentRoute, currentUserData = updatedUserData) -> {
              // The user is not logged in so push him to login page
              pushAppRoute(Url.LOGIN_EMAIL.toRoute(queryParams = calculateRedirectQueryParams())!!)
            }

            location.pathname.removeSuffix("/") == "/admin" -> {
              val clientUser = updatedUserData.clientUser!!
              when {
                UserPermission.EDIT_OWN_ACCESS in clientUser.permissions -> pushAppRoute(Url.ACCESS_MANAGEMENT_LIST.toRoute()!!)
                UserPermission.EDIT_LOCATIONS in clientUser.permissions -> pushAppRoute(Url.LOCATIONS_LIST.toRoute()!!)
                UserPermission.VIEW_CHECKINS in clientUser.permissions -> pushAppRoute(Url.REPORT.toRoute()!!)
                UserPermission.EDIT_USERS in clientUser.permissions -> pushAppRoute(Url.USERS.toRoute()!!)
              }
            }

            else -> {
              // User linked directly to a sub-page
              handleHistoryChange(currentUserData = updatedUserData)
            }
          }
        }

        onCleanup {
          // TODO: @mh Check if this is called when the component is unmounted or if unmounting works differently.
          console.log("onUnmount") // TODO: @mh Remove after testing
        }
      }

      val locale: () -> Localization = {
        when (activeLanguage) {
          MbLocalizedStringConfig.SupportedLanguage.De -> {
            deDE
          }

          MbLocalizedStringConfig.SupportedLanguage.En -> {
            enUS
          }
        }
      }

      val theme = useMemo(*emptyArray()) { // TODO: @mh Maybe for correctness use locale instead of emptyArray since it can change.
        // TODO: @mh Check that this is only called once.
        createTheme(
          // TODO: @mh Extract this to a separate function.
          options = jso {
            typography = jso {
              useNextVariants = true
            }
            palette = jso {
              primary = jso {
                main = ColorPalette.primaryColor
                contrastText = "#fff"
              }

              secondary = jso {
                main = ColorPalette.secondaryColor
              }

              success = jso {
                main = "#41d856"
                contrastText = "#fff"
              }

            }

            components = jso {
              val autoCompleteOff = "off" // Prevent lastpass from adding the icon for autofill
              this["MuiTooltip"] = jso {
                styleOverrides = jso {
                  tooltip = jso {
                    backgroundColor = "#616161" // Default tooltip color but without alpha to improve readability
                    fontSize = "0.875rem" // Default body font size to improve readability
                  }
                }
              }

              this["MuiAutocomplete"] = jso {
                defaultProps = jso {
                  disablePortal = true // Without this, the absolute position of the dropdown element is sometimes ~400px too high
                }
              }
              this["MuiTextField"] = jso {
                defaultProps = jso {
                  autoComplete = autoCompleteOff
                }
              }
              this["MuiInputBase"] = jso {
                defaultProps = jso {
                  autoComplete = autoCompleteOff
                }
              }
              this["MuiInput"] = jso {
                defaultProps = jso {
                  autoComplete = autoCompleteOff
                }
              }
              this["MuiOutlinedInput"] = jso {
                defaultProps = jso {
                  autoComplete = autoCompleteOff
                }
              }
              this["MuiFilledInput"] = jso {
                defaultProps = jso {
                  autoComplete = autoCompleteOff
                }
              }
              this["MuiSelect"] = jso {
                defaultProps = jso {
                  autoComplete = autoCompleteOff
                }
              }
              this["MuiNativeSelect"] = jso {
                defaultProps = jso {
                  autoComplete = autoCompleteOff
                }
              }
            }
          },
          locale(),
        )
      }

      val checkInSideDrawerItems: () -> List<SideDrawerItem> = {
        if (userData?.clientUser?.canEditAnyLocationAccess == true) {
          listOf(
            SideDrawerItem(
              label = Url.ACCESS_MANAGEMENT_LIST.title,
              icon = LockOpen,
              url = Url.ACCESS_MANAGEMENT_LIST
            ),
            SideDrawerItem(
              label = Url.GUEST_CHECK_IN.title,
              icon = ContactMail,
              url = Url.GUEST_CHECK_IN
            )
          )
        } else {
          emptyList()
        }
      }

      val moderatorSideDrawerItems: () -> List<SideDrawerItem> = {
        val items = mutableListOf<SideDrawerItem>()

        if (userData?.clientUser?.canEditLocations == true || userData?.clientUser?.canViewCheckIns == true) {
          items += SideDrawerItem(
            label = Url.LOCATIONS_LIST.title,
            icon = MeetingRoom,
            url = Url.LOCATIONS_LIST
          )
        }
        if (userData?.clientUser?.canViewCheckIns == true) {
          items += SideDrawerItem(
            label = Url.REPORT.title,
            icon = BlurCircular,
            url = Url.REPORT
          )
        }

        items
      }

      val adminSideDrawerItems: () -> List<SideDrawerItem> = {
        if (userData?.clientUser?.canEditUsers == true) {
          listOf(
            SideDrawerItem(
              label = Url.USERS.title,
              icon = People,
              url = Url.USERS
            ),
            SideDrawerItem(
              label = Url.ADMIN_INFO.title,
              icon = Info,
              url = Url.ADMIN_INFO
            ),
          )
        } else {
          emptyList()
        }
      }

      return AppController(
        userData = userData,
        loadingUserData = loadingUserData,
        currentAppRoute = currentAppRoute,
        mobileNavOpen = mobileNavOpen,
        setMobileNavOpen = setMobileNavOpen,
        activeLanguage = activeLanguage,
        theme = theme,
        snackbarRef = snackbarRef,
        navigationHandlerDialogRef = navigationHandlerDialogRef,
        checkInSideDrawerItems = checkInSideDrawerItems,
        moderatorSideDrawerItems = moderatorSideDrawerItems,
        adminSideDrawerItems = adminSideDrawerItems,
        pushAppRoute = pushAppRoute,
        handleHistoryChange = ::handleHistoryChange,
        onLangChange = ::onLangChange,
        fetchUserDataAndInit = ::fetchUserDataAndInit,
        renderViewContent = ChildrenBuilder::renderViewContent,
      )
    }
  }
}

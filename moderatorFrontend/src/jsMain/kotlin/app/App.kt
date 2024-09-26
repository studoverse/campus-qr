package app

import MbSnackbar
import com.studo.campusqr.common.UserPermission
import com.studo.campusqr.common.extensions.emptyToNull
import com.studo.campusqr.common.payloads.*
import csstype.*
import js.objects.jso
import kotlinx.browser.document
import mbSnackbar
import mui.icons.material.*
import mui.material.Box
import mui.material.styles.ThemeProvider
import mui.material.styles.createTheme
import mui.system.sx
import react.*
import react.dom.flushSync
import util.*
import util.Url
import views.common.centeredProgress
import views.common.networkErrorView
import web.cssom.*
import web.location.location
import webcore.*
import webcore.NavigationHandler.allUrls
import webcore.extensions.launch
import webcore.extensions.toRoute
import webcore.shell.AppShellConfig
import webcore.shell.appShell

val baseUrl = location.href.substringBefore("/admin")

external interface AppProps : Props

external interface AppState : State {
  // Order of initialization:
  // 1. userData and loadingUserData are set in fetchUserDataAndInit.
  // 2. currentAppRoute is set in block() of fetchUserDataAndInit. block() is only called after userData is updated.
  var userData: UserData?
  var loadingUserData: Boolean
  var currentAppRoute: AppRoute?

  var mobileNavOpen: Boolean
  var activeLanguage: MbLocalizedStringConfig.SupportedLanguage
}

private class App : RComponent<AppProps, AppState>() {
  // Only used for the NavigationHandler since this dialog must exist globally
  private var navigationHandlerDialogRef = createRef<MbDialog>()
  private var snackbarRef = createRef<MbSnackbar>()

  override fun AppState.init() {
    // TODO: @mh This is not called because the constructor is not executed anymore which is why in the render function we get:
    //  App.kt:341 Uncaught TypeError: Cannot read properties of null (reading 'activeLanguage').
    console.log("init test") // TODO: @mh Remove after testing
    userData = null
    loadingUserData = true
    currentAppRoute = null
    mobileNavOpen = false
    activeLanguage = MbLocalizedStringConfig.selectedLanguage
  }

  private val checkInSideDrawerItems: List<SideDrawerItem>
    get() {
      return if (state.userData?.clientUser?.canEditAnyLocationAccess == true) {
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

  private val moderatorSideDrawerItems: List<SideDrawerItem>
    get() {
      val items = mutableListOf<SideDrawerItem>()

      if (state.userData?.clientUser?.canEditLocations == true || state.userData?.clientUser?.canViewCheckIns == true) {
        items += SideDrawerItem(
          label = Url.LOCATIONS_LIST.title,
          icon = MeetingRoom,
          url = Url.LOCATIONS_LIST
        )
      }
      if (state.userData?.clientUser?.canViewCheckIns == true) {
        items += SideDrawerItem(
          label = Url.REPORT.title,
          icon = BlurCircular,
          url = Url.REPORT
        )
      }

      return items
    }

  private val adminSideDrawerItems: List<SideDrawerItem>
    get() {
      return if (state.userData?.clientUser?.canEditUsers == true) {
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

  private fun pushAppRoute(route: AppRoute) {
    if (NavigationHandler.shouldNavigate(route, NavigationHandler.NavigationEvent.PUSH_APP_ROUTE, ::handleHistoryChange)) {
      NavigationHandler.pushHistory(
        relativeUrl = route.relativeUrl,
        title = route.url.title.get(),
        handleHistoryChange = ::handleHistoryChange,
      )
      NavigationHandler.resetScrollPosition()
    }
  }

  private fun calculateRedirectQueryParams(): Map<String, String> = location.relativeUrl
    .removeSuffix("/")
    .takeIf { it != "/admin" && it != "/admin/login" } // Default path, no need to redirect
    ?.emptyToNull() // Do not redirect to empty url, it's safely handled in the router but the url would look strange
    ?.let { mapOf("redirect" to it) }
    ?: emptyMap()

  private fun handleHistoryChange(newRoute: AppRoute? = location.toRoute()) {
    if (newRoute == null) {
      console.log("Omit history change for 404 page")
    } else if (state.userData == null) {
      // Do not update `currentAppRoute` when `userData` is not yet initialized.
      // `isNotAuthenticatedButRequiresAuth` will then be checked again after mounting.
      // `currentAppRoute` is set after mounting.
    } else if (isNotAuthenticatedButRequiresAuth(newRoute)) {
      // The user might end here by clicking back after logging out.
      // componentDidMount() won't be called as going back is not mounting a new component.
      // The user is not logged in so push him to login page
      pushAppRoute(Url.LOGIN_EMAIL.toRoute(queryParams = calculateRedirectQueryParams())!!)
    } else {
      console.info("Change route to ${newRoute.relativeUrl}")
      setState { currentAppRoute = newRoute }
    }
  }

  private fun fetchUserDataAndInit(block: (() -> Unit)? = null) = launch {
    val fetchedUserData = NetworkManager.get<UserData>("$apiBase/user/data")

    if (fetchedUserData != null) {
      // userData needs to be set when calling `block`, so execute the state update beforehand
      flushSync {
        setState {
          userData = fetchedUserData
          loadingUserData = false
        }
      }
      block?.invoke()
    } else {
      setState {
        loadingUserData = false
      }
    }
  }

  private fun isNotAuthenticatedButRequiresAuth(currentRoute: AppRoute?): Boolean {
    return !state.userData!!.isAuthenticated && currentRoute?.url?.requiresAuth != false
  }

  override fun componentDidMount() {
    NavigationHandler.initApp(
      allUrls = Url.values().toList(),
      dialogRef = navigationHandlerDialogRef,
      handleHistoryChange = ::handleHistoryChange,
      getCurrentAppRoute = { state.currentAppRoute },
    )

    val duplicatePaths = allUrls.groupBy { it.path }.filter { it.value.count() > 1 }.keys
    if (duplicatePaths.isNotEmpty()) {
      throw IllegalStateException(
        "Duplicate path at ${duplicatePaths.first()} is not allowed by design. " +
            "We need a 1:1 mapping of AppRoutes and paths"
      )
    }
    fetchUserDataAndInit {
      // Do not use state.currentAppRoute here, because it's not set yet.
      // currentAppRoute will be set in this function through pushAppRoute/handleHistoryChange.
      val currentRoute = location.toRoute()

      when {
        isNotAuthenticatedButRequiresAuth(currentRoute) -> {
          // The user is not logged in so push him to login page
          pushAppRoute(Url.LOGIN_EMAIL.toRoute(queryParams = calculateRedirectQueryParams())!!)
        }

        location.pathname.removeSuffix("/") == "/admin" -> {
          val clientUser = state.userData!!.clientUser!!
          when {
            UserPermission.EDIT_OWN_ACCESS in clientUser.permissions -> pushAppRoute(Url.ACCESS_MANAGEMENT_LIST.toRoute()!!)
            UserPermission.EDIT_LOCATIONS in clientUser.permissions -> pushAppRoute(Url.LOCATIONS_LIST.toRoute()!!)
            UserPermission.VIEW_CHECKINS in clientUser.permissions -> pushAppRoute(Url.REPORT.toRoute()!!)
            UserPermission.EDIT_USERS in clientUser.permissions -> pushAppRoute(Url.USERS.toRoute()!!)
          }
        }

        else -> {
          // User linked directly to a sub-page
          handleHistoryChange()
        }
      }
    }
  }

  private val locale
    get() = when (state.activeLanguage) {
      MbLocalizedStringConfig.SupportedLanguage.De -> {
        deDE
      }

      MbLocalizedStringConfig.SupportedLanguage.En -> {
        enUS
      }
    }

  private val theme = createTheme(
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
    locale,
  )

  private fun ChildrenBuilder.renderViewContent() {
    if (state.loadingUserData || (location.toRoute() != null && state.currentAppRoute == null)) {
      // Wait for the network request in fetchUserDataAndInit() to complete or wait for currentAppRoute to be set if the route exists.
      // Path not found is handled in renderAppContent()
      centeredProgress()
    } else if (state.userData == null) {
      networkErrorView()
    } else {
      renderAppContent()
    }
  }

  private fun onLangChange(newLang: MbLocalizedStringConfig.SupportedLanguage) {
    setState {
      activeLanguage = newLang
    }
    MbLocalizedStringConfig.selectedLanguage = newLang
  }

  override fun ChildrenBuilder.render() {
    document.body?.style?.backgroundColor = "white"
    console.log("render") // TODO: @mh Remove after testing
    Box {
      // TODO: @mh Remove after testing
      //  When just showing this box, "everything" works.
      +"Test"
    }
    // TODO: @mh Comment in again after testing with the base case.
    /*ThemeProvider {
      this.theme = this@App.theme
      appContextToInject.Provider(
        AppContext(
          languageContext = LanguageContext(state.activeLanguage, ::onLangChange),
          snackbarRef = snackbarRef,
          routeContext = RouteContext(state.currentAppRoute, ::pushAppRoute),
          themeContext = ThemeContext(this@App.theme),
          userDataContext = UserDataContext(userData = state.userData, state.loadingUserData, ::fetchUserDataAndInit)
        )
      ) {
        // Global components
        mbSnackbar(ref = snackbarRef)
        mbDialog(ref = navigationHandlerDialogRef)

        // Render content without side drawer and toolbar, if no shell option is activated via url hash
        if (location.hash.contains("noShell") ||
          state.currentAppRoute?.url?.showWithShell != true
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
            renderViewContent()
          }
        } else {
          appShell(
            config = AppShellConfig(
              appBarElevation = 0,
              mobileNavOpen = state.mobileNavOpen,
              mobileNavOpenChange = { mobileNavOpen ->
                setState { this.mobileNavOpen = mobileNavOpen }
              },
              smallToolbar = true,
              stickyNavigation = true,
              viewContent = {
                renderViewContent()
              },
              drawerList = {
                renderAppDrawerItems(
                  config = AppDrawerItemsConfig(
                    checkInSideDrawerItems = if (state.loadingUserData) emptyList() else checkInSideDrawerItems,
                    moderatorSideDrawerItems = if (state.loadingUserData) emptyList() else moderatorSideDrawerItems,
                    adminSideDrawerItems = if (state.loadingUserData) emptyList() else adminSideDrawerItems,
                    loading = false,
                    mobileNavOpenChange = { mobileNavOpen ->
                      setState { this.mobileNavOpen = mobileNavOpen }
                    }
                  )
                )
              },
              toolbarIcon = null,
              hideDrawer = false,
              themeColor = this@App.theme.palette.primary.main,
            )
          )
        }
      }
    }*/
  }
}

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

fun ChildrenBuilder.app() {
  App::class.react {}
}

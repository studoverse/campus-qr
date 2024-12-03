package app

import MbSnackbarRef
import com.studo.campusqr.common.UserPermission
import com.studo.campusqr.common.extensions.emptyToNull
import com.studo.campusqr.common.payloads.UserData
import com.studo.campusqr.common.payloads.canEditAnyLocationAccess
import com.studo.campusqr.common.payloads.canEditLocations
import com.studo.campusqr.common.payloads.canEditUsers
import com.studo.campusqr.common.payloads.canViewCheckIns
import com.studo.campusqr.common.payloads.isAuthenticated
import js.objects.jso
import mui.icons.material.BlurCircular
import mui.icons.material.ContactMail
import mui.icons.material.Info
import mui.icons.material.LockOpen
import mui.icons.material.MeetingRoom
import mui.icons.material.People
import mui.material.styles.Theme
import mui.material.styles.createTheme
import react.ChildrenBuilder
import react.MutableRefObject
import react.useEffect
import react.useEffectOnce
import react.useMemo
import react.useRef
import react.useState
import util.AppRoute
import util.MbLocalizedStringConfig
import util.Url
import util.apiBase
import util.get
import util.relativeUrl
import views.common.CenteredProgress
import views.common.networkErrorView
import web.location.location
import webcore.Launch
import webcore.Localization
import webcore.MbDialogRef
import webcore.NavigationHandler
import webcore.NavigationHandler.allUrls
import webcore.NetworkManager
import webcore.deDE
import webcore.enUS
import webcore.extensions.toRoute

data class AppController(
  val userData: UserData?,
  val loadingUserData: Boolean,
  val currentAppRoute: AppRoute?,
  val mobileNavOpen: Boolean,
  val mobileNavOpenChange: (mobileNavOpen: Boolean) -> Unit,
  val activeLanguage: MbLocalizedStringConfig.SupportedLanguage,
  val onLangChange: (newLang: MbLocalizedStringConfig.SupportedLanguage) -> Unit,
  val theme: Theme,
  val snackbarRef: MutableRefObject<MbSnackbarRef>,
  val navigationHandlerDialogRef: MutableRefObject<MbDialogRef>,
  val checkInSideDrawerItems: List<SideDrawerItem>,
  val moderatorSideDrawerItems: List<SideDrawerItem>,
  val adminSideDrawerItems: List<SideDrawerItem>,
  val pushAppRoute: (AppRoute) -> Unit,
  val fetchUserDataAndInit: (block: ((UserData) -> Unit)?) -> Unit,
  val renderViewContent: ChildrenBuilder.() -> Unit,
) {
  companion object {
    fun use(launch: Launch): AppController {
      var userData: UserData? by useState(null)
      var userDataRef = useRef(userData) // Needed to avoid function closure issues in handleHistoryChange
      var loadingUserData: Boolean by useState(true)
      var currentAppRoute: AppRoute? by useState(null)
      var currentAppRouteRef = useRef(currentAppRoute) // Needed to avoid function closure issues in getCurrentAppRoute
      var mobileNavOpen: Boolean by useState(false)
      var activeLanguage: MbLocalizedStringConfig.SupportedLanguage by useState(MbLocalizedStringConfig.selectedLanguage)

      // Only used for the NavigationHandler since this dialog must exist globally
      var navigationHandlerDialogRef = useRef<MbDialogRef>()
      var snackbarRef = useRef<MbSnackbarRef>()

      lateinit var pushAppRoute: (AppRoute) -> Unit // Workaround for function not being visible for handleHistoryChange.

      fun handleHistoryChange(newRoute: AppRoute? = location.toRoute(), currentUserData: UserData? = userDataRef.current) {
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
          userData = fetchedUserData
          loadingUserData = false

          block?.invoke(fetchedUserData)
        } else {
          loadingUserData = false
        }
      }

      fun ChildrenBuilder.renderViewContent() {
        if (loadingUserData || (location.toRoute() != null && currentAppRoute == null)) {
          // Wait for the network request in fetchUserDataAndInit() to complete or wait for currentAppRoute to be set if the route exists.
          // Path not found is handled in renderAppContent()
          CenteredProgress {}
        } else if (userData == null) {
          networkErrorView()
        } else {
          AppContent {}
        }
      }

      fun mobileNavOpenChange(newMobileNavOpen: Boolean) {
        mobileNavOpen = newMobileNavOpen
      }

      useEffectOnce {
        NavigationHandler.initApp(
          allUrls = Url.entries,
          dialogRef = navigationHandlerDialogRef,
          handleHistoryChange = ::handleHistoryChange,
          getCurrentAppRoute = { currentAppRouteRef.current },
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
      }

      useEffect(userData) {
        // Always keep ref up to date.
        userDataRef.current = userData
      }

      useEffect(currentAppRoute) {
        // Always keep ref up to date.
        currentAppRouteRef.current = currentAppRoute
      }

      val locale: Localization = useMemo(activeLanguage) {
        when (activeLanguage) {
          MbLocalizedStringConfig.SupportedLanguage.De -> {
            deDE
          }

          MbLocalizedStringConfig.SupportedLanguage.En -> {
            enUS
          }
        }
      }

      val theme = useGetTheme(locale)

      val checkInSideDrawerItems: List<SideDrawerItem> = useMemo(userData) {
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

      val moderatorSideDrawerItems: List<SideDrawerItem> = useMemo(userData) {
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

      val adminSideDrawerItems: List<SideDrawerItem> = useMemo(userData) {
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
        mobileNavOpenChange = ::mobileNavOpenChange,
        activeLanguage = activeLanguage,
        onLangChange = ::onLangChange,
        theme = theme,
        snackbarRef = snackbarRef,
        navigationHandlerDialogRef = navigationHandlerDialogRef,
        checkInSideDrawerItems = checkInSideDrawerItems,
        moderatorSideDrawerItems = moderatorSideDrawerItems,
        adminSideDrawerItems = adminSideDrawerItems,
        pushAppRoute = pushAppRoute,
        fetchUserDataAndInit = ::fetchUserDataAndInit,
        renderViewContent = ChildrenBuilder::renderViewContent,
      )
    }
  }
}

private fun useGetTheme(locale: Localization): Theme {
  return useMemo(*arrayOf(locale)) {
    createTheme(
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

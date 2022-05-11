package app

import com.studo.campusqr.common.UserPermission
import com.studo.campusqr.common.extensions.emptyToNull
import com.studo.campusqr.common.payloads.*
import csstype.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.js.jso
import mui.icons.material.*
import mui.material.Box
import mui.material.styles.ThemeProvider
import mui.material.styles.createTheme
import mui.system.sx
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.Node
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.url.URL
import react.*
import react.dom.flushSync
import util.*
import util.Url
import views.common.centeredProgress
import views.common.networkErrorView
import webcore.*
import webcore.extensions.findParent
import webcore.extensions.launch
import webcore.shell.AppShellConfig
import webcore.shell.appShell

val baseUrl = window.location.href.substringBefore("/admin")

external interface AppProps : Props

external interface AppState : State {
  var userData: UserData?
  var loadingUserData: Boolean
  var currentAppRoute: AppRoute
  var mobileNavOpen: Boolean
  var activeLanguage: MbLocalizedStringConfig.SupportedLanguage
}

private class App : RComponent<AppProps, AppState>() {

  private var snackbarRef = createRef<MbSnackbar>()
  private var dialogRef = createRef<MbMaterialDialog>()

  override fun AppState.init() {
    userData = null
    loadingUserData = true
    currentAppRoute = AppRoute(Url.BLANK)
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

  private fun enableClientSideRouting() {
    window.addEventListener("popstate", {
      handleHistoryChange()
    })

    window.addEventListener("click", { event ->
      val mouseEvent = event as MouseEvent
      val target = mouseEvent.target
      if (target != null && !mouseEvent.altKey && !mouseEvent.ctrlKey && !mouseEvent.metaKey && !mouseEvent.shiftKey) {
        val linkNode = (target as Node).findParent { it.nodeName.lowercase() == "a" } ?: return@addEventListener
        val anchor = linkNode as HTMLAnchorElement
        val parsedUrl = try {
          URL(anchor.href)
        } catch (_: Throwable) {
          null
        }
        val relativeUrl = parsedUrl?.relativeUrl
        val activeView = parsedUrl?.toRoute()
        if (relativeUrl != null && anchor.target != "_blank" && activeView != null) {
          if (relativeUrl != window.location.relativeUrl) {
            window.history.pushState(null, anchor.title, relativeUrl)
            handleHistoryChange()
          }
          event.preventDefault()
        }
      }
    })
  }

  private fun pushAppRoute(route: AppRoute) {
    if (route.relativeUrl != window.location.relativeUrl) {
      window.history.pushState(data = null, title = route.url.title.get(), url = route.relativeUrl)
    }
    handleHistoryChange()
  }

  private fun calculateRedirectQueryParams(): Map<String, String> = window.location.relativeUrl
    .removeSuffix("/")
    .takeIf { it != "/admin" && it != "/admin/login" } // Default path, no need to redirect
    ?.emptyToNull() // Do not redirect to empty url, it's safely handled in the router but the url would look strange
    ?.let { mapOf("redirect" to it) }
    ?: emptyMap()

  private fun handleHistoryChange() {
    val activeView = window.location.toRoute() ?: return

    if (isNotAuthenticatedButRequiresAuth(activeView)) {
      // The user might end here by clicking back after logging out.
      // componentDidMount() won't be called as going back is not mounting a new component.
      // The user is not logged in so push him to login page
      pushAppRoute(Url.LOGIN_EMAIL.toRoute(queryParams = calculateRedirectQueryParams())!!)
    } else {
      setState { currentAppRoute = activeView }
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
    fetchUserDataAndInit {
      // Do not use state.currentAppRoute here, because that can represent the old route and not the new location
      val currentRoute = window.location.toRoute()

      when {
        isNotAuthenticatedButRequiresAuth(currentRoute) -> {
          // The user is not logged in so push him to login page
          pushAppRoute(Url.LOGIN_EMAIL.toRoute(queryParams = calculateRedirectQueryParams())!!)
        }
        window.location.pathname.removeSuffix("/") == "/admin" -> {
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

      enableClientSideRouting()
    }
  }

  private val theme = createTheme(jso {

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
  })

  private fun ChildrenBuilder.renderViewContent() {
    if (state.userData == null) {
      if (state.loadingUserData) {
        Box {
          sx {
            display = Display.flex
            minHeight = 100.vh
            flexDirection = FlexDirection.column
          }
          centeredProgress()
        }
      } else {
        networkErrorView()
      }
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

  private fun showSnackbar(text: String) {
    snackbarRef.current!!.showSnackbar(text)
  }

  private fun showSnackbarAdvanced(config: MbSnackbarConfig) {
    snackbarRef.current!!.showSnackbar(config)
  }

  private fun showDialog(dialogConfig: DialogConfig) {
    dialogRef.current!!.showDialog(dialogConfig)
  }

  private fun closeDialog() {
    dialogRef.current!!.closeDialog()
  }

  override fun ChildrenBuilder.render() {
    document.body?.style?.backgroundColor = "white"
    ThemeProvider {
      this.theme = this@App.theme
      appContext.Provider(
        AppContext(
          languageContext = LanguageContext(state.activeLanguage, ::onLangChange),
          snackbarContext = MbSnackbarContext(::showSnackbar, ::showSnackbarAdvanced),
          dialogContext = MbDialogContext(::showDialog, ::closeDialog),
          routeContext = RouteContext(state.currentAppRoute, ::pushAppRoute),
          themeContext = ThemeContext(this@App.theme),
          userDataContext = UserDataContext(userData = state.userData, state.loadingUserData, ::fetchUserDataAndInit)
        )
      ) {
        // Global components
        mbSnackbar(ref = snackbarRef)
        mbMaterialDialog(ref = dialogRef)

        // Render content without side drawer and toolbar, if no shell option is activated via url hash
        if (window.location.hash.contains("noShell") || window.location.pathname.startsWith("/admin/login")) {
          renderViewContent()
        } else {
          appShell(
            config = AppShellConfig(
              appBarElevation = 0,
              mobileNavOpen = state.mobileNavOpen,
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
                    onCloseMobileNav = {
                      setState { mobileNavOpen = false }
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
    }
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

package app

import MuiPickersUtilsProvider
import Url
import apiBase
import com.studo.campusqr.common.*
import com.studo.campusqr.common.extensions.emptyToNull
import kotlinext.js.js
import kotlinx.browser.document
import kotlinx.browser.window
import luxonUtils
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.Node
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.url.URL
import react.*
import react.dom.div
import util.*
import views.common.centeredProgress
import views.common.networkErrorView
import webcore.NetworkManager
import webcore.extensions.findParent
import webcore.extensions.launch
import webcore.materialUI.*
import webcore.shell.appShell

data class LanguageState(
  val activeLanguage: MbLocalizedStringConfig.SupportedLanguage,
  val onLanguageChange: (newLang: MbLocalizedStringConfig.SupportedLanguage) -> Unit
)

val baseUrl = window.location.href.substringBeforeLast("/admin")

data class RouteContext(val pushRoute: (AppRoute) -> Unit)

val languageContext = createContext(LanguageState(MbLocalizedStringConfig.selectedLanguage) {})
val routeContext = createContext<RouteContext>()

interface AppProps : RProps {
  var classes: AppStyles
}

interface AppState : RState {
  var userData: UserData?
  var loadingUserData: Boolean
  var currentAppRoute: AppRoute
  var mobileNavOpen: Boolean
  var activeLanguage: MbLocalizedStringConfig.SupportedLanguage
}

class App : RComponent<AppProps, AppState>() {

  private val checkInSideDrawerItems = listOf(
    SideDrawerItem(
      label = Url.ACCESS_MANAGEMENT_LIST.title,
      icon = lockOpenIcon,
      url = Url.ACCESS_MANAGEMENT_LIST
    ),
    SideDrawerItem(
      label = Url.GUEST_CHECK_IN.title,
      icon = contactMailIcon,
      url = Url.GUEST_CHECK_IN
    )
  )

  private val moderatorSideDrawerItems: List<SideDrawerItem> get() {
    val items = mutableListOf<SideDrawerItem>()

    if (state.userData?.clientUser?.isLocationManager == true) {
      items += SideDrawerItem(
          label = Url.LOCATIONS_LIST.title,
          icon = meetingRoomIcon,
          url = Url.LOCATIONS_LIST
      )
    }
    if (state.userData?.clientUser?.isInfectionManager == true) {
      items += SideDrawerItem(
          label = Url.REPORT.title,
          icon = blurCircularIcon,
          url = Url.REPORT
      )
    }

    return items
  }

  private val adminSideDrawerItems = listOf(
    SideDrawerItem(
      label = Url.USERS.title,
      icon = peopleIcon,
      url = Url.USERS
    ),
    SideDrawerItem(
      label = Url.ADMIN_INFO.title,
      icon = infoIcon,
      url = Url.ADMIN_INFO
    ),
  )

  private fun enableClientSideRouting() {
    window.addEventListener("popstate", {
      handleHistoryChange()
    })

    window.addEventListener("click", { event ->
      val mouseEvent = event as MouseEvent
      val target = mouseEvent.target
      if (target != null && !mouseEvent.altKey && !mouseEvent.ctrlKey && !mouseEvent.metaKey && !mouseEvent.shiftKey) {
        val linkNode = (target as Node).findParent { it.nodeName.toLowerCase() == "a" } ?: return@addEventListener
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

  private fun handleHistoryChange() {
    val activeView = window.location.toRoute() ?: return
    setState { currentAppRoute = activeView }
  }

  override fun AppState.init() {
    userData = null
    loadingUserData = true
    currentAppRoute = AppRoute(Url.BLANK)
    mobileNavOpen = false
    activeLanguage = MbLocalizedStringConfig.selectedLanguage
  }

  private fun fetchUserDataAndInit(block: () -> Unit) = launch {
    val fetchedUserData = NetworkManager.get<UserData>("$apiBase/user/data")

    if (fetchedUserData != null) {
      setState {
        userData = fetchedUserData
        loadingUserData = false
      }
      block()
    } else {
      setState {
        loadingUserData = false
      }
    }
  }

  override fun componentDidMount() {
    fetchUserDataAndInit {
      // Do not use state.currentAppRoute here, because that can represent the old route and not the new location
      val currentRoute = window.location.toRoute()

      fun calculateRedirectQueryParams(): Map<String, String> = window.location.relativeUrl
          .removeSuffix("/")
          .takeIf { it != "/admin" && it != "/admin/login" } // Default path, no need to redirect
          ?.emptyToNull() // Do not redirect to empty url, it's safely handled in the router but the url would look strange
        ?.let { mapOf("redirect" to it) }
        ?: emptyMap()

      when {
        !state.userData!!.isAuthenticated && currentRoute?.url?.requiresAuth != false -> {
          // The user is not logged in so push him to login page
          pushAppRoute(Url.LOGIN_EMAIL.toRoute(queryParams = calculateRedirectQueryParams())!!)
        }
        window.location.pathname.removeSuffix("/") == "/admin" -> {
          val clientUser = state.userData!!.clientUser!!
          when {
            UserRole.ACCESS_MANAGER in clientUser.roles -> pushAppRoute(Url.ACCESS_MANAGEMENT_LIST.toRoute()!!)
            UserRole.LOCATION_MANAGER in clientUser.roles -> pushAppRoute(Url.LOCATIONS_LIST.toRoute()!!)
            UserRole.INFECTION_MANAGER in clientUser.roles -> pushAppRoute(Url.REPORT.toRoute()!!)
            UserRole.ADMIN in clientUser.roles -> pushAppRoute(Url.USERS.toRoute()!!)
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

  private val theme = createMuiTheme(js {
    this.typography = js {
      useNextVariants = true
    }
    palette = js {

      // White
      default = js {
        main = ColorPalette.default
      }

      primary = js {
        main = ColorPalette.primaryColor
        contrastText = "#fff"
      }

      secondary = js {
        main = ColorPalette.secondaryColor
      }

      affirmative = js {
        main = "#41d856"
        contrastText = "#fff"
      }

    }
  })

  private fun RBuilder.renderViewContent() {
    if (state.userData == null) {
      if (state.loadingUserData) {
        div(this@App.props.classes.verticalCentered) {
          centeredProgress()
        }
      } else {
        networkErrorView()
      }
    } else {
      renderAppContent(AppContentProps.Config(
        currentAppRoute = state.currentAppRoute,
        userData = state.userData
      ))
    }
  }

  private fun onLangChange(newLang: MbLocalizedStringConfig.SupportedLanguage) {
    setState {
      activeLanguage = newLang
    }
    MbLocalizedStringConfig.selectedLanguage = newLang
  }

  override fun RBuilder.render() {
    document.body?.style?.backgroundColor = "white"
    muiThemeProvider {
      attrs.theme = theme

      MuiPickersUtilsProvider {
        attrs.utils = luxonUtils
        routeContext.Provider(RouteContext(::pushAppRoute)) {
          languageContext.Provider(LanguageState(state.activeLanguage, ::onLangChange)) {
            // Render content without side drawer and toolbar, if no shell option is activated via url hash
            if (window.location.hash.contains("noShell") || window.location.pathname.startsWith("/admin/login")) {
              renderViewContent()
            } else {
              appShell {
                attrs.appBarElevation = 0
                attrs.mobileNavOpen = state.mobileNavOpen
                attrs.smallToolbar = true
                attrs.stickyNavigation = true
                attrs.viewContent = {
                  renderViewContent()
                }
                attrs.drawerList = {
                  renderAppDrawerItems(AppDrawerItemsProps.Config(
                    userData = state.userData,
                    currentAppRoute = state.currentAppRoute,
                    checkInSideDrawerItems = if (state.loadingUserData) emptyList() else checkInSideDrawerItems,
                    moderatorSideDrawerItems = if (state.loadingUserData) emptyList() else moderatorSideDrawerItems,
                    adminSideDrawerItems = if (state.loadingUserData) emptyList() else adminSideDrawerItems,
                    loading = false,
                    onCloseMobileNav = {
                      setState { mobileNavOpen = false }
                    }
                  ))
                }
              }
            }
          }
        }
      }
    }
  }
}

interface AppStyles : RProps {
  var verticalCentered: String
}

private val styles = { theme: dynamic ->
  js {
    verticalCentered = js {
      display = "flex"
      minHeight = "100vh"
      flexDirection = "column"
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
}

private val styledApp = withStyles<AppProps, App>(styles = styles)

fun RBuilder.app() = styledApp {}

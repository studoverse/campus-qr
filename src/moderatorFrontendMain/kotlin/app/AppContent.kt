package app

import Url
import com.studo.campusqr.common.UserData
import kotlinext.js.js
import kotlinx.browser.window
import pathBase
import react.*
import react.dom.div
import util.AppRoute
import util.Strings
import util.get
import views.accessManagement.accessManagementOverview.renderAccessManagementList
import views.adminInfo.renderAdminInfo
import views.common.pathNotFoundView
import views.locations.locationsOverview.renderListLocations
import views.login.LoginMode
import views.login.renderLoginView
import views.report.renderReport
import views.users.AddUserProps
import views.users.renderAddUser
import views.users.renderUsers
import webcore.MbSnackbarProps
import webcore.materialUI.withStyles
import webcore.mbSnackbar

interface AppContentProps : RProps {
  class Config(
    val currentAppRoute: AppRoute?,
    val userData: UserData?,
    val onShowSnackbar: (String) -> Unit
  )

  var config: Config

  var classes: AppContentClasses
}

interface AppContentState : RState {
  var snackbarText: String
}

class AppContent : RComponent<AppContentProps, AppContentState>() {
  override fun AppContentState.init() {
    snackbarText = ""
  }

  override fun RBuilder.render() {
    val url = props.config.currentAppRoute?.url
    val windowPath = window.location.href.substringAfter("$pathBase/")
    when (url) {
      Url.ACCESS_MANAGEMENT_LIST -> renderAccessManagementList(location = null)
      Url.LOCATIONS_LIST -> renderListLocations()
      Url.REPORT -> renderReport()
      Url.USERS -> renderUsers(userData = props.config.userData!!)
      Url.ACCOUNT_SETTINGS -> div(classes = props.classes.container) {
        mbSnackbar(
          MbSnackbarProps.Config(
            show = state.snackbarText.isNotEmpty(),
            message = state.snackbarText,
            onClose = {
              setState {
                snackbarText = ""
              }
            })
        )
        renderAddUser(
          config = AddUserProps.Config.Edit(props.config.userData!!.clientUser!!,
            onFinished = { result ->
              setState {
                snackbarText = if (result == "ok") {
                  Strings.user_updated_account_details.get()
                } else {
                  Strings.network_error.get()
                }
              }
            }
          ),
          userData = props.config.userData!!
        )
      }
      Url.ADMIN_INFO -> renderAdminInfo()
      Url.LOGIN_EMAIL -> renderLoginView(
        studoUserData = props.config.userData!!,
        mode = LoginMode.EMAIL
      )
      Url.BLANK -> Unit
      null -> pathNotFoundView()
      else -> throw IllegalStateException("Path not found: ${url.path}")
    }
  }
}

interface AppContentClasses {
  // Keep in sync with AppContentStyle!
  var container: String
}

private val AppContentStyle = { theme: dynamic ->
  // Keep in sync with AppContentClasses!
  js {
    container = js {
      marginTop = 32
      marginLeft = 32
      marginRight = marginLeft
      marginBottom = 32
    }
  }
}

private val styled = withStyles<AppContentProps, AppContent>(AppContentStyle)

fun RBuilder.renderAppContent(config: AppContentProps.Config) = styled {
  this.attrs.config = config
}

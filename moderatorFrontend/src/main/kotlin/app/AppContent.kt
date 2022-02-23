package app

import com.studo.campusqr.common.payloads.UserData
import kotlinext.js.js
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import util.AppRoute
import util.Url
import views.accessManagement.accessManagementExport.renderAccessManagementExportList
import views.accessManagement.accessManagementOverview.renderAccessManagementList
import views.adminInfo.renderAdminInfo
import views.common.pathNotFoundView
import views.guestCheckIn.guestCheckInOverview.renderGuestCheckInOverview
import views.locations.locationsOverview.renderListLocations
import views.login.LoginMode
import views.login.renderLoginView
import views.report.renderReport
import views.users.MyAccountConfig
import views.users.renderMyAccount
import views.users.renderUsers
import webcore.materialUI.withStyles

class AppContentConfig(
  val currentAppRoute: AppRoute?,
  val userData: UserData?,
)

external interface AppContentProps : RProps {
  var config: AppContentConfig

  var classes: AppContentClasses
}

external interface AppContentState : RState

class AppContent : RComponent<AppContentProps, AppContentState>() {

  override fun RBuilder.render() {
    val currentAppRoute = props.config.currentAppRoute

    when (currentAppRoute?.url) {
      Url.ACCESS_MANAGEMENT_LIST -> renderAccessManagementList(locationId = null)
      Url.ACCESS_MANAGEMENT_LOCATION_LIST -> renderAccessManagementList(locationId = currentAppRoute.pathParams["id"])
      Url.ACCESS_MANAGEMENT_LIST_EXPORT -> renderAccessManagementExportList(locationId = null)
      Url.ACCESS_MANAGEMENT_LOCATION_LIST_EXPORT -> renderAccessManagementExportList(locationId = currentAppRoute.pathParams["id"])
      Url.GUEST_CHECK_IN -> renderGuestCheckInOverview()
      Url.LOCATIONS_LIST -> renderListLocations(userData = props.config.userData!!)
      Url.REPORT -> renderReport()
      Url.USERS -> renderUsers(userData = props.config.userData!!)
      Url.ACCOUNT_SETTINGS -> renderMyAccount(MyAccountConfig(props.config.userData!!))
      Url.ADMIN_INFO -> renderAdminInfo()
      Url.LOGIN_EMAIL -> renderLoginView(
        studoUserData = props.config.userData!!,
        mode = LoginMode.EMAIL
      )
      Url.BLANK -> Unit
      null -> pathNotFoundView()
    }
  }
}

interface AppContentClasses

private val style = { _: dynamic ->
  js {
  }
}

private val styled = withStyles<AppContentProps, AppContent>(style)

fun RBuilder.renderAppContent(config: AppContentConfig) = styled {
  this.attrs.config = config
}

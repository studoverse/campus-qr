package app

import com.studo.campusqr.common.payloads.UserData
import react.ChildrenBuilder
import react.Props
import react.State
import react.react
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
import webcore.RComponent

class AppContentConfig(
  val currentAppRoute: AppRoute?,
  val userData: UserData?,
)

external interface AppContentProps : Props {
  var config: AppContentConfig
}

external interface AppContentState : State

private class AppContent : RComponent<AppContentProps, AppContentState>() {

  override fun ChildrenBuilder.render() {
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
      Url.ACCOUNT_SETTINGS -> renderMyAccount(config = MyAccountConfig(props.config.userData!!))
      Url.ADMIN_INFO -> renderAdminInfo()
      Url.LOGIN_EMAIL -> renderLoginView(
        userData = props.config.userData!!,
        loginMode = LoginMode.EMAIL
      )
      Url.BLANK -> Unit
      null -> pathNotFoundView()
    }

  }
}

fun ChildrenBuilder.renderAppContent(config: AppContentConfig) {
  AppContent::class.react {
    this.config = config
  }
}

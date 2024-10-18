package app

import react.*
import util.Url
import views.accessManagement.accessManagementExport.renderAccessManagementExportList
import views.accessManagement.accessManagementOverview.AccessManagementListFc
import views.adminInfo.AdminInfoFc
import views.common.pathNotFoundView
import views.guestCheckIn.guestCheckInOverview.GuestCheckInOverviewFc
import views.locations.locationsOverview.ListLocations
import views.login.LoginMode
import views.login.LoginViewFc
import views.report.renderReport
import views.users.MyAccountFc
import views.users.renderUsers
import webcore.FcWithCoroutineScope

external interface AppContentProps : Props

val AppContentFc = FcWithCoroutineScope<AppContentProps> { props, componentScope ->
  val appContext = useContext(appContextToInject)
  val currentAppRoute = appContext!!.routeContext.currentAppRoute

  when (currentAppRoute?.url) {
    Url.ACCESS_MANAGEMENT_LIST -> AccessManagementListFc { locationId = null }
    Url.ACCESS_MANAGEMENT_LOCATION_LIST -> AccessManagementListFc { locationId = currentAppRoute.pathParams["id"] }
    Url.ACCESS_MANAGEMENT_LIST_EXPORT -> renderAccessManagementExportList(locationId = null)
    Url.ACCESS_MANAGEMENT_LOCATION_LIST_EXPORT -> renderAccessManagementExportList(locationId = currentAppRoute.pathParams["id"])
    Url.GUEST_CHECK_IN -> GuestCheckInOverviewFc {}
    Url.LOCATIONS_LIST -> ListLocations {}
    Url.REPORT -> renderReport()
    Url.USERS -> renderUsers()
    Url.ACCOUNT_SETTINGS -> MyAccountFc {}
    Url.ADMIN_INFO -> AdminInfoFc {}
    Url.LOGIN_EMAIL -> LoginViewFc { loginMode = LoginMode.EMAIL }
    Url.BLANK -> +"." // Just show something here, so we don't have a blank page which would be hard for debugging
    null -> pathNotFoundView()
  }
}

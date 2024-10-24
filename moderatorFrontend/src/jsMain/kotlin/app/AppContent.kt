package app

import react.*
import util.Url
import views.accessManagement.accessManagementExport.ListAccessManagementExport
import views.accessManagement.accessManagementOverview.AccessManagementListFc
import views.adminInfo.AdminInfoFc
import views.common.pathNotFoundView
import views.guestCheckIn.guestCheckInOverview.GuestCheckInOverviewFc
import views.locations.locationsOverview.ListLocations
import views.login.LoginMode
import views.login.LoginViewFc
import views.report.ReportFc
import views.users.listUsers.ListUsers
import views.users.MyAccountFc
import webcore.FcWithCoroutineScope

external interface AppContentProps : Props

val AppContentFc = FcWithCoroutineScope<AppContentProps> { props, componentScope ->
  val appContext = useContext(appContextToInject)
  val currentAppRoute = appContext!!.routeContext.currentAppRoute

  when (currentAppRoute?.url) {
    Url.ACCESS_MANAGEMENT_LIST -> AccessManagementListFc { locationId = null }
    Url.ACCESS_MANAGEMENT_LOCATION_LIST -> AccessManagementListFc { locationId = currentAppRoute.pathParams["id"] }
    Url.ACCESS_MANAGEMENT_LIST_EXPORT -> ListAccessManagementExport { locationId = null }
    Url.ACCESS_MANAGEMENT_LOCATION_LIST_EXPORT -> ListAccessManagementExport { locationId = currentAppRoute.pathParams["id"] }
    Url.GUEST_CHECK_IN -> GuestCheckInOverviewFc {}
    Url.LOCATIONS_LIST -> ListLocations {}
    Url.REPORT -> ReportFc {}
    Url.USERS -> ListUsers {}
    Url.ACCOUNT_SETTINGS -> MyAccountFc {}
    Url.ADMIN_INFO -> AdminInfoFc {}
    Url.LOGIN_EMAIL -> LoginViewFc { loginMode = LoginMode.EMAIL }
    Url.BLANK -> +"." // Just show something here, so we don't have a blank page which would be hard for debugging
    null -> pathNotFoundView()
  }
}

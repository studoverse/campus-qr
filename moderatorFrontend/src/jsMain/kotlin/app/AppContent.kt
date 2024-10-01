package app

import react.*
import util.Url
import views.accessManagement.accessManagementExport.renderAccessManagementExportList
import views.accessManagement.accessManagementOverview.renderAccessManagementList
import views.adminInfo.renderAdminInfo
import views.common.pathNotFoundView
import views.guestCheckIn.guestCheckInOverview.renderGuestCheckInOverview
import views.locations.locationsOverview.renderListLocations
import views.login.LoginMode
import views.login.LoginViewFc
import views.report.renderReport
import views.users.renderMyAccount
import views.users.renderUsers
import webcore.FcWithCoroutineScope

external interface AppContentProps : Props

val AppContentFc = FcWithCoroutineScope { props: AppContentProps, componentScope ->
  val appContext = useContext(appContextToInject)
  useEffectOnceWithCleanup { // TODO: @mh Maybe emptyArray can be replaced with listOf() ?
    console.log("onMount") // TODO: @mh Remove after testing

    onCleanup {
      // TODO: @mh Check if this is called when the component is unmounted or if unmounting works differently.
      console.log("onUnmount") // TODO: @mh Remove after testing
    }
  }

  val currentAppRoute = appContext!!.routeContext.currentAppRoute

  when (currentAppRoute?.url) {
    Url.ACCESS_MANAGEMENT_LIST -> renderAccessManagementList(locationId = null)
    Url.ACCESS_MANAGEMENT_LOCATION_LIST -> renderAccessManagementList(locationId = currentAppRoute.pathParams["id"])
    Url.ACCESS_MANAGEMENT_LIST_EXPORT -> renderAccessManagementExportList(locationId = null)
    Url.ACCESS_MANAGEMENT_LOCATION_LIST_EXPORT -> renderAccessManagementExportList(locationId = currentAppRoute.pathParams["id"])
    Url.GUEST_CHECK_IN -> renderGuestCheckInOverview()
    Url.LOCATIONS_LIST -> renderListLocations()
    Url.REPORT -> renderReport()
    Url.USERS -> renderUsers()
    Url.ACCOUNT_SETTINGS -> renderMyAccount()
    Url.ADMIN_INFO -> renderAdminInfo()
    Url.LOGIN_EMAIL -> LoginViewFc { loginMode = LoginMode.EMAIL }
    Url.BLANK -> +"." // Just show something here, so we don't have a blank page which would be hard for debugging
    null -> pathNotFoundView()
  }
}

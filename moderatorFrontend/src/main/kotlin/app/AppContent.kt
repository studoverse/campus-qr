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
import views.login.renderLoginView
import views.report.renderReport
import views.users.renderMyAccount
import views.users.renderUsers
import webcore.RComponent

external interface AppContentProps : Props

external interface AppContentState : State

private class AppContent : RComponent<AppContentProps, AppContentState>() {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(AppContent::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  override fun ChildrenBuilder.render() {
    val currentAppRoute = appContext.routeContext.currentAppRoute

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
      Url.LOGIN_EMAIL -> renderLoginView(loginMode = LoginMode.EMAIL)
      Url.BLANK -> Unit
      null -> pathNotFoundView()
    }

  }
}

fun ChildrenBuilder.renderAppContent() {
  AppContent::class.react {}
}

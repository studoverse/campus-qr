package app

import react.*
import react.Suspense
import util.Url
import views.accessManagement.accessManagementExport.listAccessManagementExport.ListAccessManagementExport
import views.accessManagement.accessManagementExport.listAccessManagementExport.ListAccessManagementExportConfig
import views.accessManagement.accessManagementOverview.AccessManagementListConfig
import views.accessManagement.accessManagementOverview.AccessManagementList
import views.adminInfo.AdminInfo
import views.common.PathNotFound
import views.guestCheckIn.guestCheckInOverview.GuestCheckInOverview
import views.locations.listLocations.ListLocations
import views.login.LoginViewConfig
import views.login.LoginViewConfig.Companion.LoginMode
import views.login.LoginView
import views.report.Report
import views.users.listUsers.ListUsers
import views.users.MyAccount
import webcore.FcWithCoroutineScope

external interface AppContentProps : Props

val AppContent = FcWithCoroutineScope<AppContentProps> { props, componentScope ->
  val appContext = use(appContextToInject)
  val currentAppRoute = appContext!!.routeContext.currentAppRoute

  when (currentAppRoute?.url) {
    Url.ACCESS_MANAGEMENT_LIST -> Suspense { AccessManagementList { config = AccessManagementListConfig(locationId = null) } }
    Url.ACCESS_MANAGEMENT_LOCATION_LIST -> {
      Suspense {
        AccessManagementList {
          config = AccessManagementListConfig(locationId = currentAppRoute.pathParams["id"])
        }
      }
    }

    Url.ACCESS_MANAGEMENT_LIST_EXPORT -> Suspense {
      ListAccessManagementExport {
        config = ListAccessManagementExportConfig(locationId = null)
      }
    }

    Url.ACCESS_MANAGEMENT_LOCATION_LIST_EXPORT -> {
      Suspense {
        ListAccessManagementExport {
          config = ListAccessManagementExportConfig(locationId = currentAppRoute.pathParams["id"])
        }
      }
    }
    Url.GUEST_CHECK_IN -> Suspense { GuestCheckInOverview {} }
    Url.LOCATIONS_LIST -> Suspense { ListLocations {} }
    Url.REPORT -> Suspense { Report {} }
    Url.USERS -> Suspense { ListUsers {} }
    Url.ACCOUNT_SETTINGS -> Suspense { MyAccount {} }
    Url.ADMIN_INFO -> Suspense { AdminInfo {} }
    Url.LOGIN_EMAIL -> Suspense { LoginView { config = LoginViewConfig(loginMode = LoginMode.EMAIL) } }
    Url.BLANK -> +"." // Just show something here, so we don't have a blank page which would be hard for debugging
    null -> Suspense { PathNotFound {} }
  }
}

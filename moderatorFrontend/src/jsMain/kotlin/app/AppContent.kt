package app

import react.*
import react.Suspense
import util.Url
import views.accessManagement.accessManagementExport.listAccessManagementExport.ListAccessManagementExport
import views.accessManagement.accessManagementExport.listAccessManagementExport.ListAccessManagementExportConfig
import views.accessManagement.accessManagementOverview.AccessManagementListConfig
import views.accessManagement.accessManagementOverview.AccessManagementListFc
import views.adminInfo.AdminInfoFc
import views.common.PathNotFoundFc
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
    Url.ACCESS_MANAGEMENT_LIST -> Suspense { AccessManagementListFc { config = AccessManagementListConfig(locationId = null) } }
    Url.ACCESS_MANAGEMENT_LOCATION_LIST -> {
      Suspense {
        AccessManagementListFc {
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
    Url.GUEST_CHECK_IN -> Suspense { GuestCheckInOverviewFc {} }
    Url.LOCATIONS_LIST -> Suspense { ListLocations {} }
    Url.REPORT -> Suspense { ReportFc {} }
    Url.USERS -> Suspense { ListUsers {} }
    Url.ACCOUNT_SETTINGS -> Suspense { MyAccountFc {} }
    Url.ADMIN_INFO -> Suspense { AdminInfoFc {} }
    Url.LOGIN_EMAIL -> Suspense { LoginViewFc { loginMode = LoginMode.EMAIL } }
    Url.BLANK -> +"." // Just show something here, so we don't have a blank page which would be hard for debugging
    null -> Suspense { PathNotFoundFc {} }
  }
}

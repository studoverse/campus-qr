package app

import com.studo.campusqr.common.payloads.UserData
import kotlinx.js.jso
import react.*
import util.AppRoute
import util.Url
import views.adminInfo.renderAdminInfo
import views.common.pathNotFoundView

class AppContentConfig(
  val currentAppRoute: AppRoute?,
  val userData: UserData?,
)

external interface AppContentProps : Props {
  var config: AppContentConfig
}

external interface AppContentState : State

class AppContent : Component<AppContentProps, AppContentState>() {

  override fun render(): ReactNode {
    return Fragment.create {
      val currentAppRoute = props.config.currentAppRoute

      when (currentAppRoute?.url) {
        /*Url.ACCESS_MANAGEMENT_LIST -> renderAccessManagementList(locationId = null)
        Url.ACCESS_MANAGEMENT_LOCATION_LIST -> renderAccessManagementList(locationId = currentAppRoute.pathParams["id"])
        Url.ACCESS_MANAGEMENT_LIST_EXPORT -> renderAccessManagementExportList(locationId = null)
        Url.ACCESS_MANAGEMENT_LOCATION_LIST_EXPORT -> renderAccessManagementExportList(locationId = currentAppRoute.pathParams["id"])
        Url.GUEST_CHECK_IN -> renderGuestCheckInOverview()
        Url.LOCATIONS_LIST -> renderListLocations(userData = props.config.userData!!)
        Url.REPORT -> renderReport()
        Url.USERS -> renderUsers(userData = props.config.userData!!)
        Url.ACCOUNT_SETTINGS -> renderMyAccount(MyAccountConfig(props.config.userData!!))*/
        Url.ADMIN_INFO -> renderAdminInfo {}
        /*Url.LOGIN_EMAIL -> renderLoginView(
          studoUserData = props.config.userData!!,
          mode = LoginMode.EMAIL
        )*/
        Url.BLANK -> Unit
        null -> pathNotFoundView {}
        else -> {} // TODO: @mh Remove after migration
      }
    }
  }
}

fun ChildrenBuilder.renderAppContent(handler: AppContentProps.() -> Unit) {
  AppContent::class.react {
    +jso(handler)
  }
}

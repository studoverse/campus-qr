package views.guestCheckIn.guestCheckInOverview

import app.appContextToInject
import com.studo.campusqr.common.payloads.ActiveCheckIn
import kotlinx.coroutines.Job
import react.useEffectOnce
import react.useState
import util.Strings
import util.apiBase
import util.get
import webcore.Launch
import webcore.NetworkManager

import kotlin.collections.toList

data class GuestCheckInOverviewController(
  val activeGuestCheckIns: List<ActiveCheckIn>?,
  val loadingCheckInList: Boolean,
  val fetchActiveGuestCheckIns: () -> Job,
) {
  companion object {
    fun use(launch: Launch): GuestCheckInOverviewController {
      val appContext = react.use(appContextToInject)!!

      var activeGuestCheckIns: List<ActiveCheckIn>? by useState<List<ActiveCheckIn>?>(null)
      var loadingCheckInList: Boolean by useState(false)

      fun fetchActiveGuestCheckIns() = launch {

        loadingCheckInList = true
        val response = NetworkManager.get<Array<ActiveCheckIn>>("$apiBase/report/listActiveGuestCheckIns")
        if (response != null) {
          activeGuestCheckIns = response.toList()
        } else {
          appContext.showSnackbarText(Strings.error_try_again.get())
        }
        loadingCheckInList = false
      }

      useEffectOnce {
        fetchActiveGuestCheckIns()
      }

      return GuestCheckInOverviewController(
        activeGuestCheckIns = activeGuestCheckIns,
        loadingCheckInList = loadingCheckInList,
        fetchActiveGuestCheckIns = ::fetchActiveGuestCheckIns,
      )
    }

    // If seat is not null, id gets appended with '-' to locationId
    fun locationIdWithSeat(locationId: String, seat: Int?) = "$locationId${seat?.let { "-$it" } ?: ""}"
  }
}
package views.accessManagement.accessManagementExport

import com.studo.campusqr.common.payloads.AccessManagementExportData
import com.studo.campusqr.common.payloads.ClientLocation
import kotlinx.coroutines.Job
import react.useEffect
import react.useEffectOnce
import react.useState
import util.apiBase
import webcore.Launch
import webcore.NetworkManager

data class ListAccessManagementExportController(
  var permitList: List<AccessManagementExportData.Permit>?,
  var clientLocation: ClientLocation?,
  var loadingPermitList: Boolean,
  val fetchAccessManagementList: () -> Job
) {
  companion object {
    fun useListAccessManagementExportController(props: ListAccessManagementExportProps, launch: Launch):
        ListAccessManagementExportController {
      var permitList: List<AccessManagementExportData.Permit>? by useState(null)
      var clientLocation: ClientLocation? by useState(null)
      var loadingPermitList: Boolean by useState(false)

      fun fetchAccessManagementList() = launch {
        loadingPermitList = true
        val params = props.locationId?.let { "?locationId=$it" } ?: ""
        val response = NetworkManager.get<AccessManagementExportData>("$apiBase/access/export$params")
        permitList = response?.permits?.toList()
        clientLocation = response?.clientLocation
        loadingPermitList = false
      }

      fun resetState() {
        permitList = null
        clientLocation = null
        loadingPermitList = false
      }

      useEffect(props.locationId) {
        resetState()
        fetchAccessManagementList()
      }

      useEffectOnce {
        fetchAccessManagementList()
      }

      return ListAccessManagementExportController(
        permitList = permitList,
        clientLocation = clientLocation,
        loadingPermitList = loadingPermitList,
        fetchAccessManagementList = ::fetchAccessManagementList
      )
    }
  }
}
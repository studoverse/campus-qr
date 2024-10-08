package views.accessManagement.accessManagementOverview

import com.studo.campusqr.common.payloads.AccessManagementData
import com.studo.campusqr.common.payloads.ClientAccessManagement
import com.studo.campusqr.common.payloads.ClientLocation
import kotlinx.coroutines.Job
import react.useEffect
import react.useEffectOnce
import react.useState
import util.apiBase
import webcore.Launch
import webcore.NetworkManager

data class ListAccessManagementController(
  val accessManagementList: List<ClientAccessManagement>?,
  val clientLocation: ClientLocation?,
  val showAccessManagementImportDialog: Boolean,
  val loadingAccessManagementList: Boolean,
  val initState: () -> Unit,
  val fetchAccessManagementList: () -> Job,
) {
  companion object {
    fun useListAccessManagementController(locationId: String?, launch: Launch): ListAccessManagementController {
      var accessManagementList: List<ClientAccessManagement>? by useState(null)
      var clientLocation: ClientLocation? by useState(null)
      var showAccessManagementImportDialog: Boolean by useState(false)
      var loadingAccessManagementList: Boolean by useState(false)

      fun initState() {
        accessManagementList = null
        clientLocation = null
        showAccessManagementImportDialog = false
        loadingAccessManagementList = false
      }

      fun fetchAccessManagementList() = launch {
        loadingAccessManagementList = true
        val params = locationId?.let { "?locationId=$it" } ?: ""
        val response = NetworkManager.get<AccessManagementData>("$apiBase/access/list$params")
        accessManagementList = response?.accessManagement?.toList()
        clientLocation = response?.clientLocation
        loadingAccessManagementList = false
      }

      useEffectOnce {
        fetchAccessManagementList()
      }

      useEffect(locationId) {
        initState()
        fetchAccessManagementList()
      }

      return ListAccessManagementController(
        accessManagementList = accessManagementList,
        clientLocation = clientLocation,
        showAccessManagementImportDialog = showAccessManagementImportDialog,
        loadingAccessManagementList = loadingAccessManagementList,
        initState = ::initState,
        fetchAccessManagementList = ::fetchAccessManagementList,
      )
    }
  }
}
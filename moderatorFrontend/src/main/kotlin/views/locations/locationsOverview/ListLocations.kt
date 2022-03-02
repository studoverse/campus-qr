package views.locations.locationsOverview

import com.studo.campusqr.common.payloads.ClientLocation
import com.studo.campusqr.common.payloads.UserData
import com.studo.campusqr.common.payloads.canEditLocations
import com.studo.campusqr.common.payloads.canViewCheckIns
import kotlinext.js.js
import kotlinx.browser.window
import react.*
import util.Strings
import util.apiBase
import util.get
import views.common.*
import views.locations.AddLocationConfig
import views.locations.renderAddLocation
import webcore.*
import webcore.extensions.launch
import webcore.materialUI.*

external interface ListLocationsProps : Props {
  var classes: ListLocationsClasses
  var userData: UserData
}

external interface ListLocationsState : State {
  var locationList: List<ClientLocation>?
  var showAddLocationDialog: Boolean
  var showImportLocationDialog: Boolean
  var loadingLocationList: Boolean
  var snackbarText: String
}

class ListLocations : RComponent<ListLocationsProps, ListLocationsState>() {

  override fun ListLocationsState.init() {
    locationList = null
    showAddLocationDialog = false
    showImportLocationDialog = false
    loadingLocationList = false
    snackbarText = ""
  }

  private fun fetchLocationList() = launch {
    setState { loadingLocationList = true }
    val response = NetworkManager.get<Array<ClientLocation>>("$apiBase/location/list")
    setState {
      locationList = response?.toList()
      loadingLocationList = false
    }
  }

  override fun componentDidMount() {
    fetchLocationList()
  }

  private fun handleCreateOrEditLocationResponse(response: String?, successText: String) {
    setState {
      snackbarText = when (response) {
        "ok" -> {
          fetchLocationList()
          showAddLocationDialog = false
          successText
        }
        else -> Strings.error_try_again.get()
      }
    }
  }

  private fun RBuilder.renderAddLocationDialog() = mbMaterialDialog(
    show = state.showAddLocationDialog,
    title = Strings.location_add.get(),
    customContent = {
      renderAddLocation(
        AddLocationConfig.Create(
          onFinished = { response ->
            handleCreateOrEditLocationResponse(response, successText = Strings.location_created.get())
          }
        )
      )
    },
    buttons = null,
    onClose = {
      setState {
        showAddLocationDialog = false
      }
    }
  )

  private fun RBuilder.renderImportButtonDialog() {

    fun closeDialog() {
      setState {
        showImportLocationDialog = false
      }
    }

    mbMaterialDialog(
      show = state.showImportLocationDialog,
      title = Strings.location_import.get(),
      textContent = Strings.location_import_details.get(),
      buttons = listOf(
        DialogButton(Strings.more_about_studo.get(), onClick = {
          closeDialog()
          window.open("https://studo.com", "_blank")
        }),
        DialogButton("OK", onClick = ::closeDialog)
      ),
      onClose = ::closeDialog
    )
  }

  private fun RBuilder.renderSnackbar() = mbSnackbar(
    MbSnackbarConfig(
      show = state.snackbarText.isNotEmpty(),
      message = state.snackbarText,
      onClose = {
        setState { snackbarText = "" }
      })
  )

  override fun RBuilder.render() {
    renderAddLocationDialog()
    renderImportButtonDialog()
    renderSnackbar()
    renderToolbarView(
      ToolbarViewConfig(
        title = Strings.locations.get(),
        buttons = listOfNotNull(
          ToolbarViewButton(
            text = Strings.print_checkout_code.get(),
            variant = "outlined",
            onClick = {
              window.open("/location/qr-codes/checkout", "_blank")
            }
          ),
          ToolbarViewButton(
            text = Strings.print_all_qrcodes.get(),
            variant = "outlined",
            onClick = {
              window.open("/location/qr-codes", "_blank")
            }
          ),
          if (props.userData.clientUser!!.canEditLocations) {
            ToolbarViewButton(
              text = Strings.location_import.get(),
              variant = "outlined",
              onClick = {
                setState {
                  showImportLocationDialog = true
                }
              }
            )
          } else null,
          if (props.userData.clientUser!!.canEditLocations) {
            ToolbarViewButton(
              text = Strings.location_create.get(),
              variant = "contained",
              onClick = {
                setState {
                  showAddLocationDialog = true
                }
              }
            )
          } else null,
        )
      )
    )

    renderLinearProgress(state.loadingLocationList)

    if (state.locationList?.isNotEmpty() == true) {
      mTable {
        mTableHead {
          mTableRow {
            mTableCell { +Strings.location_name.get() }
            if (props.userData.clientUser!!.canViewCheckIns) {
              mTableCell { +Strings.location_check_in_count.get() }
            }
            mTableCell { +Strings.location_access_type.get() }
            mTableCell { +Strings.location_number_of_seats.get() }
            mTableCell { +Strings.actions.get() }
          }
        }
        mTableBody {
          state.locationList!!.forEach { location ->
            renderLocationTableRow(
              LocationTableRowConfig(
                location,
                onEditFinished = { response ->
                  handleCreateOrEditLocationResponse(response, Strings.location_edited.get())
                },
                onDeleteFinished = { response ->
                  handleCreateOrEditLocationResponse(response, Strings.location_deleted.get())
                },
                userData = props.userData,
              )
            )
          }
        }
      }
    } else if (state.locationList == null && !state.loadingLocationList) {
      networkErrorView()
    } else if (!state.loadingLocationList) {
      genericErrorView(
        Strings.location_no_locations_title.get(),
        Strings.location_no_locations_subtitle.get()
      )
    }
  }
}

external interface ListLocationsClasses

private val style = { _: dynamic ->
  js {
  }
}

private val styled = withStyles<ListLocationsProps, ListLocations>(style)

fun RBuilder.renderListLocations(userData: UserData) = styled {
  // Set component attrs here
  attrs.userData = userData
}
  
package views.locations.locationsOverview

import com.studo.campusqr.common.payloads.ClientLocation
import com.studo.campusqr.common.payloads.UserData
import com.studo.campusqr.common.payloads.canEditLocations
import com.studo.campusqr.common.payloads.canViewCheckIns
import kotlinx.browser.window
import kotlinx.js.jso
import mui.material.*
import react.ChildrenBuilder
import react.Props
import react.State
import react.react
import util.Strings
import util.apiBase
import util.get
import views.common.*
import views.locations.AddLocationConfig
import views.locations.renderAddLocation
import webcore.*
import webcore.extensions.launch

external interface ListLocationsProps : Props {
  var userData: UserData
}

external interface ListLocationsState : State {
  var locationList: List<ClientLocation>?
  var showAddLocationDialog: Boolean
  var showImportLocationDialog: Boolean
  var loadingLocationList: Boolean
  var snackbarText: String
}

private class ListLocations : RComponent<ListLocationsProps, ListLocationsState>() {

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

  private fun ChildrenBuilder.renderAddLocationDialog() = mbMaterialDialog(handler = {
    config = MbMaterialDialogConfig(
      show = state.showAddLocationDialog,
      title = Strings.location_add.get(),
      customContent = {
        renderAddLocation {
          config = AddLocationConfig.Create(
            onFinished = { response ->
              handleCreateOrEditLocationResponse(response, successText = Strings.location_created.get())
            }
          )
        }
      },
      buttons = null,
      onClose = {
        setState {
          showAddLocationDialog = false
        }
      }
    )
  })

  private fun ChildrenBuilder.renderImportButtonDialog() {

    fun closeDialog() {
      setState {
        showImportLocationDialog = false
      }
    }

    mbMaterialDialog(handler = {
      config = MbMaterialDialogConfig(
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
    })
  }

  private fun ChildrenBuilder.renderSnackbar() = mbSnackbar {
    config = MbSnackbarConfig(
      show = state.snackbarText.isNotEmpty(),
      message = state.snackbarText,
      onClose = {
        setState { snackbarText = "" }
      }
    )
  }

  override fun ChildrenBuilder.render() {
    renderAddLocationDialog()
    renderImportButtonDialog()
    renderSnackbar()
    renderToolbarView {
      config = ToolbarViewConfig(
        title = Strings.locations.get(),
        buttons = listOfNotNull(
          ToolbarButton(
            text = Strings.print_checkout_code.get(),
            variant = ButtonVariant.outlined,
            onClick = {
              window.open("/location/qr-codes/checkout", "_blank")
            }
          ),
          ToolbarButton(
            text = Strings.print_all_qrcodes.get(),
            variant = ButtonVariant.outlined,
            onClick = {
              window.open("/location/qr-codes", "_blank")
            }
          ),
          if (props.userData.clientUser!!.canEditLocations) {
            ToolbarButton(
              text = Strings.location_import.get(),
              variant = ButtonVariant.outlined,
              onClick = {
                setState {
                  showImportLocationDialog = true
                }
              }
            )
          } else null,
          if (props.userData.clientUser!!.canEditLocations) {
            ToolbarButton(
              text = Strings.location_create.get(),
              variant = ButtonVariant.outlined,
              onClick = {
                setState {
                  showAddLocationDialog = true
                }
              }
            )
          } else null,
        )
      )
    }

    renderMbLinearProgress { show = state.loadingLocationList }

    if (state.locationList?.isNotEmpty() == true) {
      Table {
        TableHead {
          TableRow {
            TableCell { +Strings.location_name.get() }
            if (props.userData.clientUser!!.canViewCheckIns) {
              TableCell { +Strings.location_check_in_count.get() }
            }
            TableCell { +Strings.location_access_type.get() }
            TableCell { +Strings.location_number_of_seats.get() }
            TableCell { +Strings.actions.get() }
          }
        }
        TableBody {
          state.locationList!!.forEach { location ->
            renderLocationTableRow {
              config = LocationTableRowConfig(
                location,
                onEditFinished = { response ->
                  handleCreateOrEditLocationResponse(response, Strings.location_edited.get())
                },
                onDeleteFinished = { response ->
                  handleCreateOrEditLocationResponse(response, Strings.location_deleted.get())
                },
                userData = props.userData,
              )
            }
          }
        }
      }
    } else if (state.locationList == null && !state.loadingLocationList) {
      networkErrorView()
    } else if (!state.loadingLocationList) {
      genericErrorView {
        title = Strings.location_no_locations_title.get()
        subtitle = Strings.location_no_locations_subtitle.get()
      }
    }
  }
}

fun ChildrenBuilder.renderListLocations(handler: ListLocationsProps.() -> Unit) {
  ListLocations::class.react {
    +jso(handler)
  }
}

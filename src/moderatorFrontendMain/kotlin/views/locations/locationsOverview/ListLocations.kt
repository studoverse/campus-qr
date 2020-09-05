package views.locations.locationsOverview

import apiBase
import app.GlobalCss
import com.studo.campusqr.common.ClientLocation
import kotlinext.js.js
import kotlinx.browser.window
import react.*
import react.dom.div
import util.Strings
import util.get
import views.locations.AddLocationProps.Config
import views.common.genericErrorView
import views.common.networkErrorView
import views.locations.LocationTableRowProps
import views.locations.renderAddLocation
import views.locations.renderLocationTableRow
import webcore.*
import webcore.extensions.launch
import webcore.materialUI.*

interface ListLocationsProps : RProps {
  var classes: ListLocationsClasses
}

interface ListLocationsState : RState {
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
      locationList = response?.toList() ?: emptyList()
      loadingLocationList = false
    }
  }

  override fun componentDidMount() {
    fetchLocationList()
  }

  private fun handleCreateOrEditLocationResponse(response: String?) {
    setState {
      snackbarText = when (response) {
        "ok" -> {
          fetchLocationList()
          showAddLocationDialog = false
          Strings.location_created.get()
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
            Config.Create(onFinished = { response ->
              handleCreateOrEditLocationResponse(response)
            })
        )
      },
      buttons = null,
      onClose = {
        setState {
          showAddLocationDialog = false
        }
      }
  )

  private fun RBuilder.renderImportButtonDialog(): ReactElement {

    fun closeDialog() {
      setState {
        showImportLocationDialog = false
      }
    }

    return mbMaterialDialog(
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
    MbSnackbarProps.Config(show = state.snackbarText.isNotEmpty(), message = state.snackbarText, onClose = {
      setState { snackbarText = "" }
    })
  )

  override fun RBuilder.render() {
    renderAddLocationDialog()
    renderImportButtonDialog()
    renderSnackbar()

    div(GlobalCss.flex) {
      typography {
        attrs.className = props.classes.header
        attrs.variant = "h5"
        +Strings.locations.get()
      }
      div(GlobalCss.flexEnd) {
        muiButton {
          attrs.classes = js {
            root = props.classes.button
          }
          attrs.variant = "outlined"
          attrs.color = "primary"
          attrs.onClick = {
            setState {
              window.open("/location/list/qr-codes", target = "_blank")
            }
          }
          +Strings.print_all_qrcodes.get()
        }
        muiButton {
          attrs.classes = js {
            root = props.classes.button
          }
          attrs.variant = "outlined"
          attrs.color = "primary"
          attrs.onClick = {
            setState {
              showImportLocationDialog = true
            }
          }
          +Strings.location_import.get()
        }

        muiButton {
          attrs.classes = js {
            root = props.classes.createButton
          }
          attrs.variant = "contained"
          attrs.color = "primary"
          attrs.onClick = {
            setState {
              showAddLocationDialog = true
            }
          }
          +Strings.location_create.get()
        }
      }
    }

    div(props.classes.progressHolder) {
      if (state.loadingLocationList) {
        linearProgress {}
      }
    }

    if (state.locationList?.isNotEmpty() == true) {
      mTable {
        mTableHead {
          mTableRow {
            mTableCell { +Strings.location_name.get() }
            mTableCell { +Strings.location_check_in_count.get() }
            mTableCell { +Strings.actions.get() }
          }
        }
        mTableBody {
          state.locationList!!.forEach { location ->
            renderLocationTableRow(
                LocationTableRowProps.Config(location, onEditFinished = { response ->
                  handleCreateOrEditLocationResponse(response)
                })
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

interface ListLocationsClasses {
  var header: String
  var button: String
  var createButton: String
  var progressHolder: String
  // Keep in sync with ListLocationsStyle!
}

private val ListLocationsStyle = { theme: dynamic ->
  // Keep in sync with ListLocationsClasses!
  js {
    header = js {
      margin = 16
    }
    button = js {
      marginRight = 16
      marginTop = 16
      marginBottom = 16
      marginLeft = 8
    }
    createButton = js {
      margin = 16
    }
    progressHolder = js {
      height = 8
    }
  }
}

private val styled = withStyles<ListLocationsProps, ListLocations>(ListLocationsStyle)

fun RBuilder.renderListLocations() = styled {
  // Set component attrs here
}
  
package views.locations.locationsOverview

import MenuItem
import Url
import apiBase
import app.baseUrl
import app.routeContext
import com.studo.campusqr.common.ClientLocation
import com.studo.campusqr.common.LocationVisitData
import com.studo.campusqr.common.accessTypeEnum
import kotlinx.browser.window
import materialMenu
import react.*
import util.*
import views.locations.AddLocationProps
import views.locations.renderAddLocation
import webcore.NetworkManager
import webcore.extensions.launch
import webcore.materialUI.*
import webcore.mbMaterialDialog

interface LocationTableRowProps : RProps {
  class Config(
      val location: ClientLocation,
      val onEditFinished: (response: String?, message: String) -> Unit,
  )

  var config: Config
  var classes: LocationTableRowClasses
}

interface LocationTableRowState : RState {
  var showEditLocationDialog: Boolean
  var showProgress: Boolean
}

class LocationTableRow : RComponent<LocationTableRowProps, LocationTableRowState>() {

  override fun LocationTableRowState.init() {
    showEditLocationDialog = false
    showProgress = false
  }

  private fun RBuilder.renderEditLocationDialog() = mbMaterialDialog(
      show = true,
      title = Strings.location_edit.get(),
      customContent = {
        renderAddLocation(AddLocationProps.Config.Edit(props.config.location, onFinished = { response ->
          if (response == "ok") {
            setState {
              showEditLocationDialog = false
            }
          }
          props.config.onEditFinished(response, Strings.location_edited.get())
        }))
      },
      buttons = null,
      onClose = {
        setState {
          showEditLocationDialog = false
        }
      }
  )

  override fun RBuilder.render() {
    if (state.showEditLocationDialog) {
      renderEditLocationDialog()
    }

    mTableRow {
      mTableCell {
        +props.config.location.name
      }
      mTableCell {
        +props.config.location.checkInCount.toString()
      }
      mTableCell {
        +props.config.location.accessTypeEnum.localizedString.get()
      }
      mTableCell {
        routeContext.Consumer { routeContext ->
          if (state.showProgress) {
            circularProgress {}
          } else {
            materialMenu(
                menuItems = listOf(
                    MenuItem(text = Strings.edit.get(), icon = editIcon, onClick = {
                      setState {
                        showEditLocationDialog = true
                      }
                    }),
                    MenuItem(text = Strings.locations_element_download_qr_code.get(), icon = imageRoundedIcon, onClick = {
                      window.open("$baseUrl/location/${props.config.location.id}/qr-code", target = "_blank")
                    }),
                    MenuItem(text = Strings.locations_element_simulate_scan.get(), icon = fullscreenIcon, onClick = {
                      window.open("../../campus-qr?s=1&l=" + props.config.location.id, target = "_blank")
                    }),
                    MenuItem(text = Strings.access_control.get(), icon = listIcon, onClick = {
                      routeContext.pushRoute(Url.ACCESS_MANAGEMENT_LOCATION_LIST.toRoute(pathParams = mapOf("id" to props.config.location.id))!!)
                    }),
                    MenuItem(text = Strings.locations_element_download_csv.get(), icon = cloudDownloadIcon, onClick = {
                      launch {
                        setState {
                          showProgress = true
                        }
                        val visitData =
                            NetworkManager.get<LocationVisitData>("$apiBase/location/${props.config.location.id}/visitsCsv")
                                ?: return@launch
                        fileDownload(data = visitData.csvData, fileName = visitData.csvFileName)
                        setState {
                          showProgress = false
                        }
                      }
                    }),
                    MenuItem(text = Strings.location_delete.get(), icon = deleteIcon, onClick = {
                      if (window.confirm(Strings.location_delete_are_you_sure.get())) {
                        launch {
                          val response = NetworkManager.get<String>("$apiBase/location/${props.config.location.id}/delete")
                              ?: return@launch
                          props.config.onEditFinished(response, Strings.location_deleted.get())
                        }
                      }
                    }),
                )
            )
          }
        }
      }
    }
  }
}

interface LocationTableRowClasses {
  // Keep in sync with LocationTableRowStyle!
}

private val LocationTableRowStyle = { theme: dynamic ->
  // Keep in sync with LocationTableRowClasses!
}

private val styled = withStyles<LocationTableRowProps, LocationTableRow>(LocationTableRowStyle)

fun RBuilder.renderLocationTableRow(config: LocationTableRowProps.Config) = styled {
  attrs.config = config
}
  
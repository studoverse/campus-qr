package views.locations.locationsOverview

import MenuItem
import Url
import apiBase
import app.baseUrl
import app.routeContext
import com.studo.campusqr.common.*
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
    val onEditFinished: (response: String?) -> Unit,
    val onDeleteFinished: (response: String?) -> Unit,
    val userData: UserData,
  ) {
    val clientUser: ClientUser get() = userData.clientUser!!
  }

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
        props.config.onEditFinished(response)
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
      if (props.config.clientUser.canViewCheckIns) {
        mTableCell {
          +props.config.location.checkInCount.toString()
        }
      }
      mTableCell {
        +props.config.location.accessTypeEnum.localizedString.get()
      }
      mTableCell {
        +(props.config.location.seatCount?.toString() ?: Strings.undefined.get())
      }
      mTableCell {
        routeContext.Consumer { routeContext ->
          if (state.showProgress) {
            circularProgress {}
          } else {
            materialMenu(
              menuItems = listOfNotNull(
                if (props.config.clientUser.canEditLocations) {
                  MenuItem(text = Strings.edit.get(), icon = editIcon, onClick = {
                    setState {
                      showEditLocationDialog = true
                    }
                  })
                } else null,
                MenuItem(text = Strings.locations_element_download_qr_code.get(), icon = imageRoundedIcon, onClick = {
                  window.open("$baseUrl/location/${props.config.location.id}/qr-code", target = "_blank")
                }),
                if (props.config.userData.liveCheckInsViewEnabled) {
                  MenuItem(text = Strings.live_check_ins.get(), icon = settingsBackupRestoreIcon, onClick = {
                    window.open("$apiBase/campus-qr/liveCheckIns?l=" + props.config.location.id, target = "_blank")
                  })
                } else null,
                if (props.config.clientUser.canViewCheckIns) {
                  MenuItem(text = Strings.locations_element_simulate_scan.get(), icon = fullscreenIcon, onClick = {
                    val locationIdSuffix = if (props.config.location.seatCount == null) "" else "-1" // Check-in at seat 1 if needed
                    window.open("$apiBase/campus-qr?s=1&l=" + props.config.location.id + locationIdSuffix, target = "_blank")
                  })
                } else null,
                if (props.config.clientUser.canEditAllLocationAccess) {
                  MenuItem(text = Strings.access_control.get(), icon = lockOpenIcon, onClick = {
                    routeContext.pushRoute(Url.ACCESS_MANAGEMENT_LOCATION_LIST.toRoute(pathParams = mapOf("id" to props.config.location.id))!!)
                  })
                } else null,
                if (props.config.clientUser.canViewCheckIns) {
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
                  })
                } else null,
                if (props.config.clientUser.canEditLocations) {
                  MenuItem(text = Strings.location_delete.get(), icon = deleteIcon, onClick = {
                    if (window.confirm(Strings.location_delete_are_you_sure.get())) {
                      launch {
                        val response = NetworkManager.post<String>(
                          "$apiBase/location/${props.config.location.id}/delete",
                          params = null
                        )
                        props.config.onDeleteFinished(response)
                      }
                    }
                  })
                } else null,
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
  
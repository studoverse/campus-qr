package views.locations.locationsOverview

import app.AppContext
import app.appContextToInject
import app.baseUrl
import com.studo.campusqr.common.payloads.*
import kotlinx.browser.window
import mui.icons.material.*
import mui.material.CircularProgress
import mui.material.TableCell
import mui.material.TableRow
import react.*
import util.*
import views.locations.AddLocationConfig
import webcore.*
import webcore.extensions.launch
import webcore.extensions.toRoute

class LocationTableRowConfig(
  val location: ClientLocation,
  val onEditFinished: (response: String?) -> Unit,
  val onDeleteFinished: (response: String?) -> Unit,
  val userData: UserData,
) {
  val clientUser: ClientUser get() = userData.clientUser!!
}

external interface LocationTableRowProps : Props {
  var config: LocationTableRowConfig
}

external interface LocationTableRowState : State {
  var showProgress: Boolean
}

private class LocationTableRow : RComponent<LocationTableRowProps, LocationTableRowState>() {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(LocationTableRow::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  override fun LocationTableRowState.init() {
    showProgress = false
  }

  private fun renderEditLocationDialog() = appContext.showDialog(
    DialogConfig(
      title = Strings.location_edit.get(),
      customContent = DialogConfig.CustomContent(views.locations.AddLocation::class) {
        config = AddLocationConfig.Edit(props.config.location, onFinished = { response ->
          props.config.onEditFinished(response)
        })
      },
    )
  )

  override fun ChildrenBuilder.render() {
    TableRow {
      TableCell {
        +props.config.location.name
      }
      if (props.config.clientUser.canViewCheckIns) {
        TableCell {
          +props.config.location.checkInCount.toString()
        }
      }
      TableCell {
        +props.config.location.accessType.localizedString.get()
      }
      TableCell {
        +(props.config.location.seatCount?.toString() ?: Strings.undefined.get())
      }
      TableCell {
        if (state.showProgress) {
          CircularProgress()
        } else {
          materialMenu(
            config = MaterialMenuConfig(
              menuItems = listOfNotNull(
                if (props.config.clientUser.canEditLocations) {
                  MenuItem(text = Strings.edit.get(), icon = Edit, onClick = {
                    renderEditLocationDialog()
                  })
                } else null,
                MenuItem(text = Strings.locations_element_download_qr_code.get(), icon = ImageRounded, onClick = {
                  window.open("$baseUrl/location/${props.config.location.id}/qr-code", target = "_blank")
                }),
                if (props.config.userData.liveCheckInsViewEnabled) {
                  MenuItem(text = Strings.live_check_ins.get(), icon = SettingsBackupRestore, onClick = {
                    window.open("$apiBase/campus-qr/liveCheckIns?l=" + props.config.location.id, target = "_blank")
                  })
                } else null,
                if (props.config.clientUser.canViewCheckIns) {
                  MenuItem(text = Strings.locations_element_simulate_scan.get(), icon = Fullscreen, onClick = {
                    val locationIdSuffix = if (props.config.location.seatCount == null) "" else "-1" // Check-in at seat 1 if needed
                    window.open("$apiBase/campus-qr?s=1&l=" + props.config.location.id + locationIdSuffix, target = "_blank")
                  })
                } else null,
                if (props.config.clientUser.canEditAllLocationAccess) {
                  MenuItem(text = Strings.access_control.get(), icon = LockOpen, onClick = {
                    appContext.routeContext.pushRoute(Url.ACCESS_MANAGEMENT_LOCATION_LIST.toRoute(pathParams = mapOf("id" to props.config.location.id))!!)
                  })
                } else null,
                if (props.config.clientUser.canViewCheckIns) {
                  MenuItem(text = Strings.locations_element_download_csv.get(), icon = CloudDownload, onClick = {
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
                  MenuItem(text = Strings.location_delete.get(), icon = Delete, onClick = {
                    if (window.confirm(Strings.location_delete_are_you_sure.get())) {
                      launch {
                        val response = NetworkManager.post<String>("$apiBase/location/${props.config.location.id}/delete")
                        props.config.onDeleteFinished(response)
                      }
                    }
                  })
                } else null,
              )
            )
          )
        }
      }
    }
  }
}

fun ChildrenBuilder.renderLocationTableRow(config: LocationTableRowConfig) {
  LocationTableRow::class.react {
    this.config = config
  }
}
  
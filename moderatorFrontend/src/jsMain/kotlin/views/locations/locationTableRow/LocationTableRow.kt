package views.locations.locationTableRow

import js.lazy.Lazy
import app.appContextToInject
import app.baseUrl
import com.studo.campusqr.common.payloads.*
import web.window.window
import mui.icons.material.*
import mui.material.CircularProgress
import mui.material.TableCell
import mui.material.TableRow
import react.*
import util.*
import views.locations.addLocation.AddLocation
import views.locations.addLocation.AddLocationConfig
import web.prompts.confirm
import web.window.WindowTarget
import web.window._blank
import webcore.*
import webcore.extensions.toRoute
import webcore.materialMenu.MaterialMenu
import webcore.materialMenu.MaterialMenuConfig
import webcore.materialMenu.MaterialMenuConfig.Companion.MenuItem

external interface LocationTableRowProps : Props {
  var config: LocationTableRowConfig
}

@Lazy
val LocationTableRow = FcWithCoroutineScope<LocationTableRowProps> { props, launch ->
  val appContext = use(appContextToInject)!!

  var showProgress: Boolean = false

  fun renderEditLocationDialog() = props.config.dialogRef.current!!.showDialog(
    DialogConfig(
      title = DialogConfig.Title(text = Strings.location_edit.get()),
      customContent = {
        Suspense {
          AddLocation {
            config = AddLocationConfig.Edit(
              location = props.config.location,
              dialogRef = props.config.dialogRef,
              onFinished = { response ->
                props.config.onEditFinished(response)
              },
            )
          }
        }
      },
    )
  )

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
      if (showProgress) {
        CircularProgress()
      } else {
        Suspense {
          MaterialMenu {
            config = MaterialMenuConfig(
              menuItems = listOfNotNull(
                if (props.config.clientUser.canEditLocations) {
                  MenuItem(text = Strings.edit.get(), icon = Edit, onClick = {
                    renderEditLocationDialog()
                  })
                } else null,
                MenuItem(text = Strings.locations_element_download_qr_code.get(), icon = ImageRounded, onClick = {
                  window.open("$baseUrl/location/${props.config.location.id}/qr-code", target = WindowTarget._blank)
                }),
                if (props.config.userData.liveCheckInsViewEnabled) {
                  MenuItem(text = Strings.live_check_ins.get(), icon = SettingsBackupRestore, onClick = {
                    window.open("$apiBase/campus-qr/liveCheckIns?l=" + props.config.location.id, target = WindowTarget._blank)
                  })
                } else null,
                if (props.config.clientUser.canViewCheckIns) {
                  MenuItem(text = Strings.locations_element_simulate_scan.get(), icon = Fullscreen, onClick = {
                    val locationIdSuffix = if (props.config.location.seatCount == null) "" else "-1" // Check-in at seat 1 if needed
                    window.open("$apiBase/campus-qr?s=1&l=" + props.config.location.id + locationIdSuffix, target = WindowTarget._blank)
                  })
                } else null,
                if (props.config.clientUser.canEditAllLocationAccess) {
                  MenuItem(text = Strings.access_control.get(), icon = LockOpen, onClick = {
                    appContext.routeContext.pushRoute(
                      Url.ACCESS_MANAGEMENT_LOCATION_LIST.toRoute(pathParams = mapOf("id" to props.config.location.id))!!
                    )
                  })
                } else null,
                if (props.config.clientUser.canViewCheckIns) {
                  MenuItem(text = Strings.locations_element_download_csv.get(), icon = CloudDownload, onClick = {
                    launch {
                      showProgress = true
                      val visitData =
                        NetworkManager.get<LocationVisitData>("$apiBase/location/${props.config.location.id}/visitsCsv")
                          ?: return@launch
                      fileDownload(data = visitData.csvData, fileName = visitData.csvFileName)
                      showProgress = false
                    }
                  })
                } else null,
                if (props.config.clientUser.canEditLocations) {
                  MenuItem(text = Strings.location_delete.get(), icon = Delete, onClick = {
                    if (confirm(Strings.location_delete_are_you_sure.get())) {
                      launch {
                        val response = NetworkManager.post<String>("$apiBase/location/${props.config.location.id}/delete")
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

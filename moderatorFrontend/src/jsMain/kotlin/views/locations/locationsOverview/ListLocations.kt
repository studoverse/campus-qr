package views.locations.locationsOverview

import app.appContextToInject
import com.studo.campusqr.common.payloads.ClientLocation
import com.studo.campusqr.common.payloads.canEditLocations
import com.studo.campusqr.common.payloads.canViewCheckIns
import web.window.window
import mui.material.*
import react.*
import util.Strings
import util.apiBase
import util.get
import views.common.*
import views.locations.AddLocation
import views.locations.AddLocationConfig
import web.window.WindowTarget
import webcore.*

external interface ListLocationsProps : Props

val ListLocations = FcWithCoroutineScope<ListLocationsProps> { props, launch ->
  val appContext = useContext(appContextToInject)!!
  val dialogRef = useRef<MbDialogRef>()

  var locationList: List<ClientLocation>? = null
  var loadingLocationList: Boolean = false

  // TODO: @mh Move to controller.
  fun fetchLocationList() = launch {
    loadingLocationList = true
    val response = NetworkManager.get<Array<ClientLocation>>("$apiBase/location/list")
    locationList = response?.toList()
    loadingLocationList = false
  }

  // TODO: @mh Move to controller.
  fun handleCreateOrEditLocationResponse(response: String?, successText: String) {
    val snackbarText = when (response) {
      "ok" -> {
        fetchLocationList()
        successText
      }
      else -> Strings.error_try_again.get()
    }
    appContext.showSnackbarText(snackbarText)
  }

  fun renderAddLocationDialog() = dialogRef.current!!.showDialog(
    DialogConfig(
      title = DialogConfig.Title(text = Strings.location_add.get()),
      customContent = {
        AddLocation {
          config = AddLocationConfig.Create(
            dialogRef = dialogRef,
            onFinished = { response ->
              handleCreateOrEditLocationResponse(response, successText = Strings.location_created.get())
            }
          )
        }
      },
    )
  )

  fun renderImportButtonDialog() {
    dialogRef.current!!.showDialog(
      DialogConfig(
        title = DialogConfig.Title(text = Strings.location_import.get()),
        text = Strings.location_import_details.get(),
        buttons = listOf(
          DialogButton(Strings.more_about_studo.get(), onClick = {
            window.open("https://studo.com", WindowTarget._blank)
          }),
          DialogButton("OK")
        ),
      )
    )
  }

  useEffectOnce {
    fetchLocationList()
  }

  MbDialogFc { ref = dialogRef }
  val userData = appContext.userDataContext.userData!!
  ToolbarViewFc {
    config = ToolbarViewConfig(
      title = Strings.locations.get(),
      buttons = listOfNotNull(
        ToolbarButton(
          text = Strings.print_checkout_code.get(),
          variant = ButtonVariant.outlined,
          onClick = {
            window.open("/location/qr-codes/checkout", WindowTarget._blank)
          }
        ),
        ToolbarButton(
          text = Strings.print_all_qrcodes.get(),
          variant = ButtonVariant.outlined,
          onClick = {
            window.open("/location/qr-codes", WindowTarget._blank)
          }
        ),
        if (userData.clientUser!!.canEditLocations) {
          ToolbarButton(
            text = Strings.location_import.get(),
            variant = ButtonVariant.outlined,
            onClick = {
              renderImportButtonDialog()
            }
          )
        } else null,
        if (userData.clientUser!!.canEditLocations) {
          ToolbarButton(
            text = Strings.location_create.get(),
            variant = ButtonVariant.contained,
            onClick = {
              renderAddLocationDialog()
            }
          )
        } else null,
      )
    )
  }

  MbLinearProgressFc { show = loadingLocationList }

  if (locationList?.isNotEmpty() == true) {
    Table {
      TableHead {
        TableRow {
          TableCell { +Strings.location_name.get() }
          if (userData.clientUser!!.canViewCheckIns) {
            TableCell { +Strings.location_check_in_count.get() }
          }
          TableCell { +Strings.location_access_type.get() }
          TableCell { +Strings.location_number_of_seats.get() }
          TableCell { +Strings.actions.get() }
        }
      }
      TableBody {
        locationList!!.forEach { location ->
          LocationTableRow {
            config = LocationTableRowConfig(
              location = location,
              dialogRef = dialogRef,
              onEditFinished = { response ->
                handleCreateOrEditLocationResponse(response, Strings.location_edited.get())
              },
              onDeleteFinished = { response ->
                handleCreateOrEditLocationResponse(response, Strings.location_deleted.get())
              },
              userData = userData,
            )
          }
        }
      }
    }
  } else if (locationList == null && !loadingLocationList) {
    networkErrorView()
  } else if (!loadingLocationList) {
    genericErrorView(
      title = Strings.location_no_locations_title.get(),
      subtitle = Strings.location_no_locations_subtitle.get()
    )
  }
}

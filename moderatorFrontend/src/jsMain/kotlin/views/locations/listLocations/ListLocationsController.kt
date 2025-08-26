package views.locations.listLocations

import app.RouteContext
import app.appContextToInject
import com.studo.campusqr.common.payloads.ClientLocation
import react.RefObject
import react.Suspense
import react.useEffectOnce
import react.useRef
import react.useState
import util.Strings
import util.apiBase
import util.get
import views.locations.addLocation.AddLocation
import views.locations.addLocation.AddLocationConfig
import views.locations.addLocation.AddLocationProps
import web.window.WindowTarget
import web.window._blank
import web.window.window
import webcore.DialogButton
import webcore.DialogConfig
import webcore.Launch
import webcore.MbDialogRef
import webcore.NetworkManager

data class ListLocationsController(
  val locationList: List<ClientLocation>?,
  val loadingLocationList: Boolean,
  val dialogRef: RefObject<MbDialogRef>,
  val locationImportOnClick: (RouteContext) -> Unit,
  val locationCreateOnClick: (RouteContext) -> Unit,
  val locationTableRowOnEditFinished: (String?) -> Unit,
  val locationTableRowOnDeleteFinished: (String?) -> Unit,
) {
  companion object {
    fun use(launch: Launch): ListLocationsController {
      var locationList: List<ClientLocation>? by useState(null)
      var loadingLocationList: Boolean by useState(false)

      val dialogRef = useRef<MbDialogRef>()
      val appContext = react.use(appContextToInject)!!

      fun fetchLocationList() = launch {
        loadingLocationList = true
        val response = NetworkManager.get<Array<ClientLocation>>("$apiBase/location/list")
        locationList = response?.toList()
        loadingLocationList = false
      }

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
            Suspense {
              AddLocation<AddLocationProps> {
                config = AddLocationConfig.Create(
                  dialogRef = dialogRef,
                  onFinished = { response ->
                    handleCreateOrEditLocationResponse(response, successText = Strings.location_created.get())
                  }
                )
              }
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

      fun locationImportOnClick(@Suppress("unused") context: RouteContext) {
        renderImportButtonDialog()
      }

      fun locationCreateOnClick(@Suppress("unused") context: RouteContext) {
        renderAddLocationDialog()
      }

      fun locationTableRowOnEditFinished(response: String?) {
        handleCreateOrEditLocationResponse(response, Strings.location_edited.get())
      }

      fun locationTableRowOnDeleteFinished(response: String?) {
        handleCreateOrEditLocationResponse(response, Strings.location_deleted.get())
      }

      useEffectOnce {
        fetchLocationList()
      }

      return ListLocationsController(
        locationList = locationList,
        loadingLocationList = loadingLocationList,
        dialogRef = dialogRef,
        locationImportOnClick = ::locationImportOnClick,
        locationCreateOnClick = ::locationCreateOnClick,
        locationTableRowOnEditFinished = ::locationTableRowOnEditFinished,
        locationTableRowOnDeleteFinished = ::locationTableRowOnDeleteFinished,
      )
    }
  }
}
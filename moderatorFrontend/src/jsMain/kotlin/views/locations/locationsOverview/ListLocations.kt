package views.locations.locationsOverview

import app.appContextToInject
import com.studo.campusqr.common.payloads.canEditLocations
import com.studo.campusqr.common.payloads.canViewCheckIns
import js.lazy.Lazy
import web.window.window
import mui.material.*
import react.*
import util.Strings
import util.get
import views.common.*
import views.common.ToolbarView.ToolbarButton
import views.common.ToolbarView.ToolbarViewConfig
import views.common.ToolbarView.ToolbarViewFc
import web.window.WindowTarget
import webcore.*

external interface ListLocationsProps : Props

@Lazy
val ListLocations = FcWithCoroutineScope<ListLocationsProps> { props, launch ->
  val controller = ListLocationsController.useListLocationsController(launch = launch)
  val appContext = useContext(appContextToInject)!!

  MbDialogFc { ref = controller.dialogRef }
  val userData = appContext.userDataContext.userData!!
  Suspense {
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
              onClick = controller.locationImportOnClick
            )
          } else null,
          if (userData.clientUser!!.canEditLocations) {
            ToolbarButton(
              text = Strings.location_create.get(),
              variant = ButtonVariant.contained,
              onClick = controller.locationCreateOnClick
            )
          } else null,
        )
      )
    }
  }

  MbLinearProgressFc { show = controller.loadingLocationList }

  if (controller.locationList?.isNotEmpty() == true) {
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
        Suspense {
          controller.locationList.forEach { location ->
            LocationTableRow {
              key = location.id
              config = LocationTableRowConfig(
                location = location,
                dialogRef = controller.dialogRef,
                onEditFinished = controller.locationTableRowOnEditFinished,
                onDeleteFinished = controller.locationTableRowOnDeleteFinished,
                userData = userData,
              )
            }
          }
        }
      }
    }
  } else if (controller.locationList == null && !controller.loadingLocationList) {
    networkErrorView()
  } else if (!controller.loadingLocationList) {
    Suspense {
      GenericErrorViewFc {
        config = GenericErrorViewConfig(
          title = Strings.location_no_locations_title.get(),
          subtitle = Strings.location_no_locations_subtitle.get(),
        )
      }
    }
  }
}

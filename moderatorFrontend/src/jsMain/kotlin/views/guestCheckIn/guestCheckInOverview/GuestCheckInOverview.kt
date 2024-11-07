package views.guestCheckIn.guestCheckInOverview

import js.lazy.Lazy
import mui.material.*
import react.*
import util.Strings
import util.get
import views.common.*
import views.common.ToolbarView.ToolbarButton
import views.common.ToolbarView.ToolbarView
import views.common.ToolbarView.ToolbarViewConfig
import views.common.genericErrorView.GenericErrorViewConfig
import views.common.genericErrorView.GenericErrorView
import views.guestCheckIn.addGuestCheckIn.AddGuestCheckInConfig
import views.guestCheckIn.addGuestCheckIn.AddGuestCheckIn
import views.guestCheckIn.guestCheckInRow.GuestCheckInRowConfig
import views.guestCheckIn.guestCheckInRow.GuestCheckInRow
import webcore.*

external interface GuestCheckinOverviewProps : Props

@Lazy
val GuestCheckInOverview = FcWithCoroutineScope<GuestCheckinOverviewProps> { props, launch ->
  val controller = GuestCheckInOverviewController.useGuestCheckInOverviewController(
    launch = launch
  )

  val dialogRef = useRef<MbDialogRef>()

  fun renderAddGuestCheckInDialog() = dialogRef.current!!.showDialog(
    dialogConfig = DialogConfig(
      title = DialogConfig.Title(Strings.guest_checkin_add_guest.get()),
      customContent = {
        Suspense {
          AddGuestCheckIn {
            config = AddGuestCheckInConfig(
              dialogRef = dialogRef,
              onGuestCheckedIn = {
                controller.fetchActiveGuestCheckIns
              }
            )
          }
        }
      },
    )
  )

  MbDialog { ref = dialogRef }
  Suspense {
    ToolbarView {
      config = ToolbarViewConfig(
        title = Strings.guest_checkin.get(),
        buttons = listOf(
          ToolbarButton(
            text = Strings.guest_checkin_add_guest.get(),
            variant = ButtonVariant.contained,
            onClick = {
              renderAddGuestCheckInDialog()
            }
          )
        )
      )
    }
  }
  MbLinearProgress { show = controller.loadingCheckInList }

  when {
    controller.activeGuestCheckIns?.isNotEmpty() == true -> Table {
      TableHead {
        TableRow {
          TableCell { +Strings.location_name.get() }
          TableCell { +Strings.email_address.get() }
          TableCell { +Strings.report_checkin_seat.get() }
          TableCell { }
        }
      }
      TableBody {
        controller.activeGuestCheckIns.forEach { activeCheckIn ->
          Suspense {
            GuestCheckInRow {
              config = GuestCheckInRowConfig(
                activeCheckIn = activeCheckIn,
                onCheckedOut = {
                  controller.fetchActiveGuestCheckIns()
                }
              )
            }
          }
        }
      }
    }

    controller.activeGuestCheckIns == null && !controller.loadingCheckInList -> networkErrorView()
    !controller.loadingCheckInList -> {
      Suspense {
        GenericErrorView {
          config = GenericErrorViewConfig(
            title = Strings.guest_checkin_not_yet_added_title.get(),
            subtitle = Strings.guest_checkin_not_yet_added_subtitle.get(),
          )
        }
      }
    }
  }
}

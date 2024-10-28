package views.guestCheckIn.guestCheckInOverview

import js.lazy.Lazy
import mui.material.*
import react.*
import util.Strings
import util.get
import views.common.*
import views.common.ToolbarView.ToolbarButton
import views.common.ToolbarView.ToolbarViewFc
import views.common.ToolbarView.ToolbarViewConfig
import views.guestCheckIn.AddGuestCheckInConfig
import views.guestCheckIn.AddGuestCheckInFc
import webcore.*

external interface GuestCheckinOverviewProps : Props

@Lazy
val GuestCheckInOverviewFc = FcWithCoroutineScope<GuestCheckinOverviewProps> { props, launch ->
  val controller = GuestCheckInOverviewController.useGuestCheckInOverviewController(
    launch = launch
  )

  val dialogRef = useRef<MbDialogRef>()

  fun renderAddGuestCheckInDialog() = dialogRef.current!!.showDialog(
    dialogConfig = DialogConfig(
      title = DialogConfig.Title(Strings.guest_checkin_add_guest.get()),
      customContent = {
        Suspense {
          AddGuestCheckInFc {
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

  MbDialogFc { ref = dialogRef }
  Suspense {
    ToolbarViewFc {
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
  MbLinearProgressFc { show = controller.loadingCheckInList }

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
            GuestCheckInRowFc {
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
        GenericErrorViewFc {
          config = GenericErrorViewConfig(
            title = Strings.guest_checkin_not_yet_added_title.get(),
            subtitle = Strings.guest_checkin_not_yet_added_subtitle.get(),
          )
        }
      }
    }
  }
}

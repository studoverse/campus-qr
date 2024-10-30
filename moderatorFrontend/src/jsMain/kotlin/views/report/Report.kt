package views.report

import com.studo.campusqr.common.emailSeparators
import com.studo.campusqr.common.extensions.emptyToNull
import com.studo.campusqr.common.extensions.format
import csstype.PropertiesBuilder
import js.lazy.Lazy
import web.cssom.*
import mui.material.*
import mui.material.Size
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.form
import util.Strings
import util.fileDownload
import util.get
import views.common.CenteredProgressFc
import views.common.MbLinearProgressFc
import views.common.spacer
import webcore.*
import webcore.datePicker.DatePickerConfig
import webcore.datePicker.DatePickerFc
import webcore.NavigationHandler.dialogRef
import kotlin.js.Date

external interface ReportProps : Props

@Lazy
val ReportFc = FcWithCoroutineScope<ReportProps> { props, launch ->
  val controller = ReportController.useReportController(
    launch = launch,
  )

  fun PropertiesBuilder.content() {
    margin = 16.px
  }

  MbDialogFc { ref = dialogRef }
  val now = Date()
  val showEmailAddress = controller.emailTextFieldValue.split(*emailSeparators).filter { it.isNotEmpty() }.count() > 1

  Typography {
    variant = TypographyVariant.h5
    sx {
      content()
    }
    +Strings.report.get()
  }

  Box {
    sx {
      margin = 16.px
    }
    gridContainer(GridDirection.row) {
      gridItem(GridSize(xs = 12, sm = 3), key = "reportGridItem1") {
        Suspense {
          DatePickerFc {
            config = DatePickerConfig(
              date = controller.infectionDate,
              label = Strings.report_infection_date.get(),
              helperText = Strings.report_infection_date_tip.get(),
              fullWidth = true,
              variant = FormControlVariant.outlined,
              max = now,
              onChange = controller.traceStartDatePickerOnChange,
            )
          }
        }
      }
      gridItem(GridSize(xs = 12, sm = 6), key = "reportGridItem2") {
        form {
          onSubmit = { event ->
            event.preventDefault()
            event.stopPropagation()
            if (controller.validateInput()) {
              controller.traceContacts()
            }
          }
          TextField {
            fullWidth = true
            variant = FormControlVariant.outlined
            label = Strings.report_email.get().toReactNode()
            value = controller.emailTextFieldValue
            error = controller.emailTextFieldError.isNotEmpty()
            helperText = (controller.emailTextFieldError.emptyToNull() ?: Strings.report_email_tip.get()).toReactNode()
            onChange = controller.emailTextFieldOnChange
          }
        }
      }
      gridItem(GridSize(xs = 12, sm = 3), key = "reportGridItem3") {
        Button {
          sx {
            margin = 16.px
          }
          variant = ButtonVariant.contained
          color = ButtonColor.primary
          onClick = {
            if (controller.validateInput()) {
              controller.traceContacts()
            }
          }
          +Strings.report_search.get()
        }
      }
    }
  }
  when {
    controller.reportData != null -> {
      MbLinearProgressFc { show = controller.showProgress }
      val reportData = controller.reportData!!
      Box {
        sx {
          content()
        }
        Typography {
          variant = TypographyVariant.h6
          +Strings.report_affected_people.get()
            .format(
              reportData.impactedUsersCount.toString(),
              reportData.reportedUserLocations.sumOf { it.potentialContacts }.toString(),
              reportData.startDate,
              reportData.endDate
            )
        }

        spacer(32)

        Table {
          TableHead {
            TableRow {
              if (showEmailAddress) TableCell { +Strings.report_checkin_email.get() }
              TableCell { +Strings.report_checkin_date.get() }
              TableCell { +Strings.report_checkin_location.get() }
              TableCell { +Strings.report_checkin_seat.get() }
              TableCell { +Strings.report_impacted_people.get() }
              TableCell { +Strings.report_checkin_filter.get() }
            }
          }
          TableBody {
            Suspense {
              reportData.reportedUserLocations.forEach { userLocation ->
                ReportTableRowFc {
                  config = ReportTableRowConfig(
                    userLocation = userLocation,
                    showEmailAddress = showEmailAddress,
                    dialogRef = dialogRef,
                    onApplyFilterChange = { userLocation, filteredSeats ->
                      controller.applyFilter(userLocation, filteredSeats)
                    },
                    onDeleteFilter = { userLocation ->
                      controller.deleteFilter(userLocation)
                    },
                  )
                }
              }
            }
          }
        }

        spacer(32)
        if (reportData.impactedUsersCount > 0) {
          Button {
            size = Size.small
            color = ButtonColor.primary
            onClick = {
              fileDownload(reportData.impactedUsersEmailsCsvData, reportData.impactedUsersEmailsCsvFileName)
            }
            +Strings.report_export_via_csv.get()
          }
          spacer()
        }

        if (reportData.reportedUserLocations.isNotEmpty()) {
          Button {
            size = Size.small
            color = ButtonColor.primary
            onClick = {
              fileDownload(reportData.reportedUserLocationsCsv, reportData.reportedUserLocationsCsvFileName)
            }
            +Strings.report_export_infected_user_checkins_csv.get()
          }
        }
      }
    }

    controller.showProgress -> CenteredProgressFc {}
  }
}
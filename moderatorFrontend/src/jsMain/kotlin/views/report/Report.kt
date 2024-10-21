package views.report

import com.studo.campusqr.common.emailSeparators
import com.studo.campusqr.common.extensions.emptyToNull
import com.studo.campusqr.common.extensions.format
import csstype.PropertiesBuilder
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
import webcore.NavigationHandler.dialogRef
import kotlin.js.Date

external interface ReportProps : Props

val ReportFc = FcWithCoroutineScope<ReportProps> { props, launch ->
  val reportController = ReportController.useReportController(
    launch = launch,
  )

  fun PropertiesBuilder.content() {
    margin = 16.px
  }

  MbDialogFc { ref = dialogRef }
  val now = Date()
  val showEmailAddress = reportController.emailTextFieldValue.split(*emailSeparators).filter { it.isNotEmpty() }.count() > 1

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
      gridItem(GridSize(xs = 12, sm = 3)) {
        DatePickerFc {
          config = DatePickerConfig(
            date = reportController.infectionDate,
            label = Strings.report_infection_date.get(),
            helperText = Strings.report_infection_date_tip.get(),
            fullWidth = true,
            variant = FormControlVariant.outlined,
            max = now,
            onChange = reportController.traceStartDatePickerOnChange,
          )
        }
      }
      gridItem(GridSize(xs = 12, sm = 6)) {
        form {
          onSubmit = { event ->
            event.preventDefault()
            event.stopPropagation()
            if (reportController.validateInput()) {
              reportController.traceContacts()
            }
          }
          TextField {
            fullWidth = true
            variant = FormControlVariant.outlined
            label = Strings.report_email.get().toReactNode()
            value = reportController.emailTextFieldValue
            error = reportController.emailTextFieldError.isNotEmpty()
            helperText = (reportController.emailTextFieldError.emptyToNull() ?: Strings.report_email_tip.get()).toReactNode()
            onChange = reportController.emailTextFieldOnChange
          }
        }
      }
      gridItem(GridSize(xs = 12, sm = 3)) {
        Button {
          sx {
            margin = 16.px
          }
          variant = ButtonVariant.contained
          color = ButtonColor.primary
          onClick = {
            if (reportController.validateInput()) {
              reportController.traceContacts()
            }
          }
          +Strings.report_search.get()
        }
      }
    }
  }
  when {
    reportController.reportData != null -> {
      MbLinearProgressFc { show = reportController.showProgress }
      val reportData = reportController.reportData!!
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
            reportData.reportedUserLocations.forEach { userLocation ->
              ReportTableRowFc {
                config = ReportTableRowConfig(
                  userLocation = userLocation,
                  showEmailAddress = showEmailAddress,
                  dialogRef = dialogRef,
                  onApplyFilterChange = { userLocation, filteredSeats ->
                    reportController.applyFilter(userLocation, filteredSeats)
                  },
                  onDeleteFilter = { userLocation ->
                    reportController.deleteFilter(userLocation)
                  },
                )
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

    reportController.showProgress -> CenteredProgressFc {}
  }
}
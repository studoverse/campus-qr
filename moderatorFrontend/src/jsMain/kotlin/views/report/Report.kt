package views.report

import app.AppContext
import app.appContextToInject
import com.studo.campusqr.common.emailSeparators
import com.studo.campusqr.common.extensions.emptyToNull
import com.studo.campusqr.common.extensions.format
import com.studo.campusqr.common.payloads.DeleteSeatFilter
import com.studo.campusqr.common.payloads.EditSeatFilter
import com.studo.campusqr.common.payloads.GetContactTracingReport
import com.studo.campusqr.common.payloads.ReportData
import csstype.PropertiesBuilder
import web.cssom.*
import mui.material.*
import mui.material.Size
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.form
import util.Strings
import util.apiBase
import util.fileDownload
import util.get
import views.common.centeredProgress
import views.common.renderMbLinearProgress
import views.common.spacer
import webcore.*
import webcore.extensions.addDays
import webcore.extensions.coerceAtMost
import webcore.extensions.launch
import kotlin.js.Date

external interface ReportProps : Props

external interface ReportState : State {
  var emailTextFieldValue: String
  var emailTextFieldError: String
  var reportData: ReportData?
  var showProgress: Boolean
  var infectionDate: Date
}

private class Report : RComponent<ReportProps, ReportState>() {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(Report::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  private val dialogRef = createRef<MbDialog>()

  override fun ReportState.init() {
    emailTextFieldValue = ""
    emailTextFieldError = ""
    reportData = null
    showProgress = false
    infectionDate = Date().addDays(-14)
  }

  private fun validateInput(): Boolean {
    if (state.emailTextFieldValue.isEmpty()) {
      setState {
        emailTextFieldError = Strings.parameter_cannot_be_empty_format.get().format(Strings.email_address.get())
      }
      return false
    }
    return true
  }

  private fun applyFilter(userLocation: ReportData.UserLocation, filteredSeats: List<Int>) = launch {
    setState { showProgress = true }
    // If no seats are selected, just delete the current filter
    if (filteredSeats.isEmpty()) {
      deleteFilter(userLocation)
    } else {
      val response = NetworkManager.post<String>(
        "$apiBase/location/${userLocation.locationId}/editSeatFilter",
        body = EditSeatFilter(
          seat = userLocation.seat!!,
          filteredSeats = filteredSeats
        )
      )
      setState {
        if (response == "ok") {
          // re-trace contacts
          traceContacts()
        } else {
          appContext.showSnackbar(Strings.error_try_again.get())
          showProgress = false
        }
      }
    }
  }

  private fun deleteFilter(userLocation: ReportData.UserLocation) = launch {
    setState { showProgress = true }
    val response = NetworkManager.post<String>(
      "$apiBase/location/${userLocation.locationId}/deleteSeatFilter",
      body = DeleteSeatFilter(seat = userLocation.seat!!)
    )
    setState {
      if (response == "ok") {
        // re-trace contacts
        traceContacts()
      } else {
        appContext.showSnackbar(Strings.error_try_again.get())
        showProgress = false
      }
    }
  }

  private fun traceContacts() = launch {
    setState { showProgress = true }
    val response = NetworkManager.post<ReportData>(
      "$apiBase/report/list",
      body = GetContactTracingReport(
        email = state.emailTextFieldValue,
        oldestDate = state.infectionDate.getTime()
      )
    )
    setState {
      showProgress = false
      reportData = if (response == null) {
        appContext.showSnackbar(Strings.error_try_again.get())
        null
      } else {
        response
      }
    }
  }

  override fun ChildrenBuilder.render() {
    mbDialog(ref = dialogRef)
    val now = Date()
    val showEmailAddress = state.emailTextFieldValue.split(*emailSeparators).filter { it.isNotEmpty() }.count() > 1

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
          datePicker(
            config = DatePickerConfig(
              date = state.infectionDate,
              label = Strings.report_infection_date.get(),
              helperText = Strings.report_infection_date_tip.get(),
              fullWidth = true,
              variant = FormControlVariant.outlined,
              max = now,
              onChange = { selectedDate: Date, _: Boolean ->
                setState {
                  infectionDate = selectedDate.coerceAtMost(now)
                }
              },
            )
          )
        }
        gridItem(GridSize(xs = 12, sm = 6)) {
          form {
            onSubmit = { event ->
              event.preventDefault()
              event.stopPropagation()
              if (validateInput()) {
                traceContacts()
              }
            }
            TextField {
              fullWidth = true
              variant = FormControlVariant.outlined
              label = Strings.report_email.get().toReactNode()
              value = state.emailTextFieldValue
              error = state.emailTextFieldError.isNotEmpty()
              helperText = (state.emailTextFieldError.emptyToNull() ?: Strings.report_email_tip.get()).toReactNode()
              onChange = { event ->
                val value = event.target.value
                setState {
                  emailTextFieldValue = value
                  emailTextFieldError = ""
                }
              }
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
              if (validateInput()) {
                traceContacts()
              }
            }
            +Strings.report_search.get()
          }
        }
      }
    }
    when {
      state.reportData != null -> {
        renderMbLinearProgress(show = state.showProgress)
        val reportData = state.reportData!!
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
                renderReportTableRow(
                  config = ReportTableRowConfig(
                    userLocation = userLocation,
                    showEmailAddress = showEmailAddress,
                    dialogRef = dialogRef,
                    onApplyFilterChange = this@Report::applyFilter,
                    onDeleteFilter = this@Report::deleteFilter
                  )
                )
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
      state.showProgress -> centeredProgress()
    }
  }

  private fun PropertiesBuilder.content() {
    margin = 16.px
  }
}

fun ChildrenBuilder.renderReport() {
  Report::class.react {}
}

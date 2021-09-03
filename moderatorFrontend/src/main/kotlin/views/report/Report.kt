package views.report

import com.studo.campusqr.common.emailSeparators
import com.studo.campusqr.common.extensions.emptyToNull
import com.studo.campusqr.common.extensions.format
import com.studo.campusqr.common.payloads.DeleteSeatFilter
import com.studo.campusqr.common.payloads.EditSeatFilter
import com.studo.campusqr.common.payloads.ReportData
import com.studo.campusqr.common.payloads.TraceContactsReportData
import kotlinext.js.js
import kotlinx.html.js.onSubmitFunction
import muiDatePicker
import org.w3c.dom.events.Event
import react.*
import react.dom.div
import react.dom.form
import util.Strings
import util.apiBase
import util.fileDownload
import util.get
import views.common.centeredProgress
import views.common.renderLinearProgress
import views.common.spacer
import webcore.*
import webcore.extensions.addDays
import webcore.extensions.inputValue
import webcore.extensions.launch
import webcore.materialUI.*
import kotlin.js.Date

interface ReportProps : RProps {
  var classes: ReportClasses
}

interface ReportState : RState {
  var emailTextFieldValue: String
  var emailTextFieldError: String
  var reportData: ReportData?
  var showProgress: Boolean
  var snackbarText: String
  var infectionDate: Date
}

class Report : RComponent<ReportProps, ReportState>() {

  override fun ReportState.init() {
    emailTextFieldValue = ""
    emailTextFieldError = ""
    reportData = null
    showProgress = false
    snackbarText = ""
    infectionDate = Date().addDays(-14)
  }

  private fun RBuilder.renderSnackbar() = mbSnackbar(
    MbSnackbarProps.Config(
      show = state.snackbarText.isNotEmpty(),
      message = state.snackbarText,
      onClose = {
        setState {
          snackbarText = ""
        }
      })
  )

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
          snackbarText = Strings.error_try_again.get()
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
        snackbarText = Strings.error_try_again.get()
        showProgress = false
      }
    }
  }

  private fun traceContacts() = launch {
    setState { showProgress = true }
    val response = NetworkManager.post<ReportData>(
      "$apiBase/report/list",
      body = TraceContactsReportData(
        email = state.emailTextFieldValue,
        oldestDate = state.infectionDate.getTime().toString()
      )
    )
    setState {
      showProgress = false
      if (response == null) {
        snackbarText = Strings.error_try_again.get()
        reportData = null
      } else {
        reportData = response
      }
    }
  }

  override fun RBuilder.render() {
    val showEmailAddress = state.emailTextFieldValue.split(*emailSeparators).filter { it.isNotEmpty() }.count() > 1

    renderSnackbar()
    typography {
      attrs.variant = "h5"
      attrs.className = props.classes.content
      +Strings.report.get()
    }

    div(props.classes.inputForm) {
      gridContainer(GridDirection.ROW) {
        gridItem(GridSize(xs = 12, sm = 3)) {
          muiDatePicker {
            attrs.fullWidth = true
            attrs.format = "dd.MM.yyyy"
            attrs.inputVariant = "outlined"
            attrs.label = Strings.report_infection_date.get()
            attrs.value = state.infectionDate
            attrs.helperText = Strings.report_infection_date_tip.get()
            attrs.onChange = {
              setState {
                infectionDate = it.toJSDate()
              }
            }
          }
        }
        gridItem(GridSize(xs = 12, sm = 6)) {
          form {
            attrs.onSubmitFunction = { event ->
              event.preventDefault()
              event.stopPropagation()
              if (validateInput()) {
                traceContacts()
              }
            }
            textField {
              attrs.fullWidth = true
              attrs.variant = "outlined"
              attrs.label = Strings.report_email.get()
              attrs.value = state.emailTextFieldValue
              attrs.error = state.emailTextFieldError.isNotEmpty()
              attrs.helperText = state.emailTextFieldError.emptyToNull() ?: Strings.report_email_tip.get()
              attrs.onChange = { event: Event ->
                val value = event.inputValue
                setState {
                  emailTextFieldValue = value
                  emailTextFieldError = ""
                }
              }
            }
          }
        }
        gridItem(GridSize(xs = 12, sm = 3)) {
          muiButton {
            attrs.classes = js {
              root = props.classes.searchButton
            }
            attrs.variant = "contained"
            attrs.color = "primary"
            attrs.onClick = {
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
        renderLinearProgress(state.showProgress)
        val reportData = state.reportData!!
        div(props.classes.content) {
          typography {
            attrs.variant = "h6"
            +Strings.report_affected_people.get()
              .format(
                reportData.impactedUsersCount.toString(),
                reportData.reportedUserLocations.sumOf { it.potentialContacts }.toString(),
                reportData.startDate,
                reportData.endDate
              )
          }

          spacer(32)

          mTable {
            mTableHead {
              mTableRow {
                if (showEmailAddress) mTableCell { +Strings.report_checkin_email.get() }
                mTableCell { +Strings.report_checkin_date.get() }
                mTableCell { +Strings.report_checkin_location.get() }
                mTableCell { +Strings.report_checkin_seat.get() }
                mTableCell { +Strings.report_impacted_people.get() }
                mTableCell { +Strings.report_checkin_filter.get() }
              }
            }
            mTableBody {
              reportData.reportedUserLocations.forEach { userLocation ->
                renderReportTableRow(
                  ReportTableRowProps.Config(
                    userLocation = userLocation,
                    showEmailAddress = showEmailAddress,
                    onApplyFilterChange = this@Report::applyFilter,
                    onDeleteFilter = this@Report::deleteFilter
                  )
                )
              }
            }
          }

          spacer(32)
          if (reportData.impactedUsersCount > 0) {
            muiButton {
              attrs.size = "small"
              attrs.color = "primary"
              attrs.onClick = {
                fileDownload(reportData.impactedUsersEmailsCsvData, reportData.impactedUsersEmailsCsvFileName)
              }
              +Strings.report_export_via_csv.get()
            }
            spacer()
          }

          if (reportData.reportedUserLocations.isNotEmpty()) {
            muiButton {
              attrs.size = "small"
              attrs.color = "primary"
              attrs.onClick = {
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
}

interface ReportClasses {
  var content: String
  var searchButton: String
  var inputForm: String
}

private val style = { _: dynamic ->
  js {
    content = js {
      margin = 16
    }
    searchButton = js {
      margin = 16
    }
    inputForm = js {
      margin = 16
    }
  }
}

private val styled = withStyles<ReportProps, Report>(style)

fun RBuilder.renderReport() = styled {
  // Set component attrs here
}

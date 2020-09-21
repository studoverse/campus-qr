package views.report

import apiBase
import app.GlobalCss
import com.studo.campusqr.common.ReportData
import com.studo.campusqr.common.emailSeparators
import com.studo.campusqr.common.extensions.emptyToNull
import com.studo.campusqr.common.extensions.format
import kotlinext.js.js
import kotlinx.browser.window
import muiDatePicker
import org.w3c.dom.events.Event
import react.*
import react.dom.div
import react.dom.tr
import util.Strings
import util.fileDownload
import util.get
import views.common.centeredProgress
import views.common.spacer
import webcore.*
import webcore.extensions.addDays
import webcore.extensions.inputValue
import webcore.extensions.launch
import webcore.materialUI.*
import kotlin.js.Date
import kotlin.js.json

interface ReportProps : RProps {
  var classes: ReportClasses
}

interface ReportState : RState {
  var emailTextFieldValue: String
  var emailTextFieldError: String
  var reportData: ReportData?
  var loadingList: Boolean
  var snackbarText: String
  var infectionDate: Date
}

class Report : RComponent<ReportProps, ReportState>() {

  override fun ReportState.init() {
    emailTextFieldValue = ""
    emailTextFieldError = ""
    reportData = null
    loadingList = false
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

  override fun RBuilder.render() {
    val showEmailAddress =
        state.emailTextFieldValue.split(*emailSeparators).filter { it.isNotEmpty() }.count() > 1

    renderSnackbar()
    typography {
      attrs.variant = "h5"
      attrs.className = props.classes.content
      +Strings.report.get()
    }

    div(props.classes.inputForm) {
      gridContainer(GridDirection.ROW, alignItems = "end") {
        gridItem(GridSize(xs = 12, sm = 3)) {
          muiDatePicker {
            attrs.fullWidth = true
            attrs.format = "dd.MM.yyyy"
            attrs.inputVariant = "outlined"
            attrs.label = Strings.report_infection_date.get()
            attrs.value = state.infectionDate
            attrs.onChange = {
              setState {
                infectionDate = it.toJSDate()
              }
            }
          }
        }
        gridItem(GridSize(xs = 12, sm = 6)) {
          textField {
            attrs.fullWidth = true
            attrs.variant = "outlined"
            attrs.label = Strings.report_email.get()
            attrs.type = "email"
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
        gridItem(GridSize(xs = 12, sm = 3)) {
          muiButton {
            attrs.classes = js {
              root = props.classes.searchButton
            }
            attrs.variant = "contained"
            attrs.color = "primary"
            attrs.onClick = {
              if (validateInput()) {
                launch {
                  setState { loadingList = true }
                  val response = NetworkManager.post<ReportData>(
                      "$apiBase/report/list", params = json(
                      "email" to state.emailTextFieldValue,
                      "oldestDate" to state.infectionDate.getTime().toString()
                  )
                  )
                  setState {
                    loadingList = false
                    if (response == null) {
                      snackbarText = Strings.error_try_again.get()
                      reportData = null
                    } else {
                      reportData = response
                    }
                  }
                }
              }
            }
            +Strings.report_search.get()
          }
        }
      }
    }
    when {
      state.loadingList -> centeredProgress()
      state.reportData != null -> {
        val reportData = state.reportData!!
        div(props.classes.content) {
          typography {
            attrs.variant = "h6"
            +Strings.report_affected_people.get()
                .format(reportData.impactedUsersCount.toString(), reportData.startDate, reportData.endDate)
          }

          spacer(32)

          if (reportData.impactedUsersCount > 0) {
            muiButton {
              attrs.size = "small"
              attrs.color = "primary"
              attrs.variant = "outlined"
              attrs.onClick = {
                window.open(reportData.impactedUsersMailtoLink, target = "_blank")
              }
              +Strings.report_export_via_mail.get()
            }
            spacer()
            muiButton {
              attrs.size = "small"
              attrs.color = "primary"
              attrs.variant = "outlined"
              attrs.onClick = {
                fileDownload(reportData.impactedUsersEmailsCsvData, reportData.impactedUsersEmailsCsvFileName)
              }
              +Strings.report_export_via_csv.get()
            }
            spacer()
          }
        }

        if (reportData.reportedUserLocations.isNotEmpty()) {
          div(GlobalCss.flex) {
            typography {
              attrs.className = props.classes.content
              attrs.variant = "h6"
              +Strings.report_checkins.get()
            }
          }

          if (reportData.reportedUserLocations.isNotEmpty()) {
            div(props.classes.content) {
              muiButton {
                attrs.size = "small"
                attrs.color = "primary"
                attrs.variant = "outlined"
                attrs.onClick = {
                  fileDownload(reportData.reportedUserLocationsCsv, reportData.reportedUserLocationsCsvFileName)
                }
                +Strings.report_export_infected_user_checkins_csv.get()
              }
            }
          }

          mTable {
            mTableHead {
              mTableRow {
                if (showEmailAddress) mTableCell { +Strings.report_checkin_email.get() }
                mTableCell { +Strings.report_checkin_date.get() }
                mTableCell { +Strings.report_checkin_location.get() }
                if (state.reportData?.reportedUserLocations?.any { it.locationSeatNumber != null } == true) {
                  mTableCell { +Strings.report_checkin_seat.get() }
                }
              }
            }
            mTableBody {
              reportData.reportedUserLocations.forEach { userLocation ->
                renderReportTableRow(
                    ReportTableRowProps.Config(
                        userLocation = userLocation,
                        showEmailAddress = showEmailAddress
                    )
                )
              }
            }
          }
        }
      }
    }
  }
}

interface ReportClasses {
  var content: String
  var searchButton: String
  var inputHolder: String
  var inputForm: String
  var emailTextField: String
  // Keep in sync with ReportStyle!
}

private val ReportStyle = { theme: dynamic ->
  // Keep in sync with ReportClasses!
  js {
    content = js {
      margin = 16
    }
    searchButton = js {
      margin = 16
    }
    inputHolder = js {
      margin = 8
    }
    inputForm = js {
      margin = 16
    }
    emailTextField = js {
      minWidth = 300
    }
  }
}

private val styled = withStyles<ReportProps, Report>(ReportStyle)

fun RBuilder.renderReport() = styled {
  // Set component attrs here
}
  
package views.report

import app.appContextToInject
import com.studo.campusqr.common.payloads.DeleteSeatFilter
import com.studo.campusqr.common.payloads.EditSeatFilter
import com.studo.campusqr.common.payloads.GetContactTracingReport
import com.studo.campusqr.common.payloads.ReportData
import kotlinx.coroutines.Job
import react.RefObject
import react.useRef
import react.useState
import util.Strings
import util.apiBase
import util.get
import webcore.Launch
import webcore.MbDialogRef
import webcore.NetworkManager
import webcore.TextFieldOnChange
import webcore.extensions.addDays
import webcore.extensions.coerceAtMost
import webcore.extensions.format
import kotlin.js.Date

data class ReportController(
  val dialogRef: RefObject<MbDialogRef>,
  val emailTextFieldValue: String,
  val emailTextFieldError: String,
  val reportData: ReportData?,
  val showProgress: Boolean,
  val infectionDate: Date,
  val applyFilter: (ReportData.UserLocation, List<Int>) -> Job,
  val deleteFilter: (ReportData.UserLocation) -> Job,
  val validateInput: () -> Boolean,
  val traceContacts: () -> Job,
  val traceStartDatePickerOnChange: (Date, Boolean) -> Unit,
  val emailTextFieldOnChange: TextFieldOnChange,
) {
  companion object {
    fun use(launch: Launch): ReportController {
      val appContext = react.use(appContextToInject)!!
      val dialogRef = useRef<MbDialogRef>()

      var emailTextFieldValue: String by useState("")
      var emailTextFieldError: String by useState("")
      var reportData: ReportData? by useState(null)
      var showProgress: Boolean by useState(false)
      var infectionDate: Date by useState(Date().addDays(-14))

      fun traceStartDatePickerOnChange(selectedDate: Date, isValid: Boolean) {
        infectionDate = selectedDate.coerceAtMost(selectedDate)
      }

      val emailTextFieldOnChange: TextFieldOnChange = { event ->
        val value = event.target.value
        emailTextFieldValue = value
        emailTextFieldError = ""
      }

      fun validateInput(): Boolean {
        if (emailTextFieldValue.isEmpty()) {
          emailTextFieldError = Strings.parameter_cannot_be_empty_format.get().format(Strings.email_address.get())
          return false
        }
        return true
      }

      fun traceContacts() = launch {
        showProgress = true
        val response = NetworkManager.post<ReportData>(
          "$apiBase/report/list",
          body = GetContactTracingReport(
            email = emailTextFieldValue,
            oldestDate = infectionDate.getTime()
          )
        )
        showProgress = false
        reportData = if (response == null) {
          appContext.showSnackbarText(Strings.error_try_again.get())
          null
        } else {
          response
        }
      }

      fun deleteFilter(userLocation: ReportData.UserLocation) = launch {
        showProgress = true
        val response = NetworkManager.post<String>(
          "$apiBase/location/${userLocation.locationId}/deleteSeatFilter",
          body = DeleteSeatFilter(seat = userLocation.seat!!)
        )
        if (response == "ok") {
          // re-trace contacts
          traceContacts()
        } else {
          appContext.showSnackbarText(Strings.error_try_again.get())
          showProgress = false
        }
      }

      fun applyFilter(userLocation: ReportData.UserLocation, filteredSeats: List<Int>) = launch {
        showProgress = true
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
          if (response == "ok") {
            // re-trace contacts
            traceContacts()
          } else {
            appContext.showSnackbarText(Strings.error_try_again.get())
            showProgress = false
          }
        }
      }

      return ReportController(
        dialogRef = dialogRef,
        emailTextFieldValue = emailTextFieldValue,
        emailTextFieldError = emailTextFieldError,
        reportData = reportData,
        showProgress = showProgress,
        infectionDate = infectionDate,
        applyFilter = ::applyFilter,
        deleteFilter = ::deleteFilter,
        validateInput = ::validateInput,
        traceContacts = ::traceContacts,
        traceStartDatePickerOnChange = ::traceStartDatePickerOnChange,
        emailTextFieldOnChange = emailTextFieldOnChange,
      )
    }
  }
}
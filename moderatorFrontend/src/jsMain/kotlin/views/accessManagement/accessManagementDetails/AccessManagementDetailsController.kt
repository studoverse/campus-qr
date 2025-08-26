package views.accessManagement.accessManagementDetails

import app.appContextToInject
import com.studo.campusqr.common.emailSeparators
import com.studo.campusqr.common.payloads.ClientAccessManagement
import com.studo.campusqr.common.payloads.ClientDateRange
import com.studo.campusqr.common.payloads.ClientLocation
import com.studo.campusqr.common.payloads.EditAccess
import com.studo.campusqr.common.payloads.NewAccess
import react.useEffectOnce
import react.useState
import util.Strings
import util.get
import util.apiBase
import webcore.AutocompleteOnChange
import webcore.ButtonOnClick
import webcore.Launch
import webcore.NetworkManager
import webcore.TextFieldOnChange
import webcore.extensions.addHours
import webcore.extensions.with
import webcore.extensions.*
import kotlin.js.Date

data class AccessManagementDetailsController(
  val selectedLocation: ClientLocation?,
  val selectedLocationTextFieldError: String,
  val locationNameToLocationMap: Map<String, ClientLocation>,
  val accessControlNoteTextFieldValue: String,
  val accessControlReasonTextFieldValue: String,
  val timeSlots: List<ClientDateRange>,
  val fromDateTimeSlotErrors: List<TimeSlotError>,
  val toDateTimeSlotErrors: List<TimeSlotError>,
  val personEmailTextFieldValue: String,
  val permittedPeopleList: List<String>,
  val showProgress: Boolean,
  val locationFetchInProgress: Boolean,
  val submitPermittedPeopleToState: () -> Unit,
  val locationSelectionOnChange: AutocompleteOnChange<String>,
  val noteTextFieldOnChange: TextFieldOnChange,
  val reasonTextFieldOnChange: TextFieldOnChange,
  val addTimeSlotOnClick: ButtonOnClick,
  val removeTimeSlotOnClick: (clientDateRange: ClientDateRange) -> Unit,
  val timeSlotDateFromOnChange: (date: Date, clientDateRange: ClientDateRange, now: Date, inThreeYears: Date) -> Unit,
  val timeSlotTimeFromOnChange: (date: Date, clientDateRange: ClientDateRange) -> Unit,
  val timeSlotDateToOnChange: (date: Date, clientDateRange: ClientDateRange, now: Date, inThreeYears: Date) -> Unit,
  val timeSlotTimeToOnChange: (date: Date, clientDateRange: ClientDateRange) -> Unit,
  val addPermittedPeopleOnChange: TextFieldOnChange,
  val removePermittedPeopleOnClick: (personIdentification: String) -> Unit,
  val createAccessControlOnClick: (config: AccessManagementDetailsConfig) -> Unit,
) {
  companion object {
    fun use(config: AccessManagementDetailsConfig, launch: Launch): AccessManagementDetailsController {
      var locationFetchInProgress: Boolean by useState(false)
      var showProgress: Boolean by useState(false)
      var locationNameToLocationMap: Map<String, ClientLocation> by useState(emptyMap())

      var selectedLocation: ClientLocation? by useState(null)
      var selectedLocationTextFieldError: String by useState("")

      var accessControlNoteTextFieldValue: String by useState("")
      var accessControlReasonTextFieldValue: String by useState("")
      var personEmailTextFieldValue: String by useState("")
      var permittedPeopleList: List<String> by useState(emptyList())
      var timeSlots: List<ClientDateRange> by useState(emptyList())

      var fromDateTimeSlotErrors: List<TimeSlotError> by useState(mutableListOf())
      var toDateTimeSlotErrors: List<TimeSlotError> by useState(mutableListOf())

      val appContext = react.use(appContextToInject)!!

      fun initFields(accessManagement: ClientAccessManagement?) {
        locationFetchInProgress = false
        showProgress = false
        locationNameToLocationMap = emptyMap()

        selectedLocation = null
        selectedLocationTextFieldError = ""

        accessControlNoteTextFieldValue = accessManagement?.note ?: ""
        accessControlReasonTextFieldValue = accessManagement?.reason ?: ""
        personEmailTextFieldValue = ""
        permittedPeopleList = accessManagement?.allowedEmails?.toList() ?: emptyList()

        fromDateTimeSlotErrors = listOf()
        toDateTimeSlotErrors = listOf()

        val fromDate = Date().addHours(1).with(minute = 0)
        timeSlots = accessManagement?.dateRanges?.toList() ?: listOf(
          ClientDateRange(
            from = fromDate.getTime(),
            to = fromDate.addHours(2).getTime()
          )
        )
      }

      fun getPermittedEmailsFromTextField() =
        personEmailTextFieldValue.lowercase().split(*emailSeparators).filter { it.isNotEmpty() }.map { it.trim() }

      fun fetchLocations() = launch {
        locationFetchInProgress = true
        val response = NetworkManager.get<Array<ClientLocation>>("$apiBase/location/list")
        if (response != null) {
          locationNameToLocationMap = response.associateBy { it.name }
          // Auto select current location
          val selectedLocationId = when (config) {
            is AccessManagementDetailsConfig.Details -> config.accessManagement.locationId
            is AccessManagementDetailsConfig.Edit -> config.accessManagement.locationId
            is AccessManagementDetailsConfig.Create -> config.locationId
          }
          if (selectedLocationId != null) {
            selectedLocation = locationNameToLocationMap.values.firstOrNull { it.id == selectedLocationId }
          }
        }
        locationFetchInProgress = false
      }

      fun createAccessControl() = launch {
        showProgress = true
        val response = NetworkManager.post<String>(
          url = "$apiBase/access/create",
          body = NewAccess(
            locationId = selectedLocation!!.id,
            // Add state.getPermittedEmailsFromTextField(), to make sure that any un-submitted emails get added
            allowedEmails = permittedPeopleList + getPermittedEmailsFromTextField(),
            dateRanges = timeSlots,
            note = accessControlNoteTextFieldValue,
            reason = accessControlReasonTextFieldValue
          )
        )
        showProgress = false
        (config as AccessManagementDetailsConfig.Create).onCreated()
        config.dialogRef.current!!.closeDialog()
        val snackbarText = if (response == "ok") {
          Strings.access_control_created_successfully.get()
        } else {
          Strings.error_try_again.get()
        }
        appContext.showSnackbarText(snackbarText)
      }

      fun editAccessControl() = launch {
        showProgress = true
        val accessManagementId = (config as AccessManagementDetailsConfig.Edit).accessManagement.id
        val response = NetworkManager.post<String>(
          url = "$apiBase/access/$accessManagementId/edit",
          body = EditAccess(
            locationId = selectedLocation?.id,
            // Add state.getPermittedEmailsFromTextField(), to make sure that any un-submitted emails get added
            allowedEmails = permittedPeopleList + getPermittedEmailsFromTextField(),
            dateRanges = timeSlots,
            note = accessControlNoteTextFieldValue,
            reason = accessControlReasonTextFieldValue
          )
        )
        showProgress = false
        config.onEdited(response == "ok")
        config.dialogRef.current!!.closeDialog()
      }

      fun validateInput(): Boolean {
        // Location has to be selected for creation
        if (config is AccessManagementDetailsConfig.Create && selectedLocation == null) {
          selectedLocationTextFieldError = Strings.access_control_please_select_location.get()
          return false
        }

        // At least one time slot has to be there in creation mode
        if (config is AccessManagementDetailsConfig.Create && timeSlots.isEmpty()) {
          // This shouldn't happen
          error("timeSlots empty: ${timeSlots}")
        }

        // Validate every timeslot
        timeSlots.forEach { timeSlot ->
          // End time cannot be before start time
          if (timeSlot.to < timeSlot.from) {
            toDateTimeSlotErrors = toDateTimeSlotErrors + TimeSlotError(
              text = Strings.access_control_end_date_before_start_date.get(),
              timeSlot = timeSlot,
            )
            return false
          }
        }

        return true
      }

      fun submitPermittedPeopleToState() {
        permittedPeopleList = permittedPeopleList + getPermittedEmailsFromTextField()
        personEmailTextFieldValue = ""
      }

      val locationSelectionOnChange: AutocompleteOnChange<String> = { _, value, _, _ ->
        value as String?
        selectedLocationTextFieldError = ""
        selectedLocation = value?.let { locationNameToLocationMap[it] }
      }

      val noteTextFieldOnChange: TextFieldOnChange = { event ->
        val value = event.target.value
        accessControlNoteTextFieldValue = value
      }

      val reasonTextFieldOnChange: TextFieldOnChange = { event ->
        val value = event.target.value
        accessControlReasonTextFieldValue = value
      }

      val addTimeSlotOnClick: ButtonOnClick = { event ->
        timeSlots = (timeSlots + ClientDateRange(timeSlots.last().from, timeSlots.last().to))
      }

      fun removeTimeSlotOnClick(clientDateRange: ClientDateRange) {
        timeSlots = timeSlots.filter { it != clientDateRange }
      }

      fun timeSlotDateFromOnChange(date: Date, clientDateRange: ClientDateRange, now: Date, inThreeYears: Date) {
        timeSlots = timeSlots.map { timeSlot ->
          if (timeSlot == clientDateRange) {
            val startDateBefore = Date(clientDateRange.from)
            val from = date.with(
              hour = startDateBefore.getHours(),
              minute = startDateBefore.getMinutes(),
              second = startDateBefore.getSeconds(),
              millisecond = startDateBefore.getMilliseconds()
            ).coerceAtMost(inThreeYears).getTime()

            // Default end date is start date + 2h
            val to = if (from >= clientDateRange.to) {
              date.coerceAtMost(inThreeYears).addHours(2).getTime()
            } else clientDateRange.to
            ClientDateRange(
              from = from,
              to = to
            )
          } else timeSlot
        }
      }

      fun timeSlotTimeFromOnChange(time: Date, clientDateRange: ClientDateRange) {
        timeSlots = timeSlots.map { timeSlot ->
          if (timeSlot == clientDateRange) {
            val startDateBefore = Date(clientDateRange.from)
            val from = time.with(
              year = startDateBefore.getFullYear(),
              month = startDateBefore.getMonth(),
              day = startDateBefore.getDate()
            ).getTime()
            // Default end date is start date + 2h
            val to = if (from >= clientDateRange.to) {
              time.addHours(2).getTime()
            } else clientDateRange.to
            ClientDateRange(
              from = from,
              to = to
            )
          } else timeSlot
        }
      }

      fun timeSlotDateToOnChange(date: Date, clientDateRange: ClientDateRange, now: Date, inThreeYears: Date) {
        timeSlots = timeSlots.map { timeSlot ->
          if (timeSlot == clientDateRange) {
            val endDateBefore = Date(clientDateRange.to)
            ClientDateRange(
              from = clientDateRange.from,
              to = date.with(
                hour = endDateBefore.getHours(),
                minute = endDateBefore.getMinutes(),
                second = endDateBefore.getSeconds(),
                millisecond = endDateBefore.getMilliseconds()
              ).coerceAtMost(inThreeYears).getTime()
            )
          } else timeSlot
        }
      }

      fun timeSlotTimeToOnChange(date: Date, clientDateRange: ClientDateRange) {
        timeSlots = timeSlots.map { timeSlot ->
          if (timeSlot == clientDateRange) {
            val endDateBefore = Date(clientDateRange.to)
            ClientDateRange(
              from = clientDateRange.from,
              to = date.with(
                year = endDateBefore.getFullYear(),
                month = endDateBefore.getMonth(),
                day = endDateBefore.getDate()
              ).getTime()
            )
          } else timeSlot
        }
      }

      val addPermittedPeopleOnChange: TextFieldOnChange = { event ->
        val value: String = event.target.value
        personEmailTextFieldValue = value
      }

      fun removePermittedPeopleOnClick(personIdentification: String) {
        permittedPeopleList = permittedPeopleList.filter { it != personIdentification }
      }

      fun createAccessControlOnClick(config: AccessManagementDetailsConfig) {
        // Reset errors from previous submit attempt
        fromDateTimeSlotErrors = listOf()
        toDateTimeSlotErrors = listOf()

        if (validateInput()) {
          when (config) {
            is AccessManagementDetailsConfig.Create -> createAccessControl()
            is AccessManagementDetailsConfig.Edit -> editAccessControl()
            else -> Unit
          }
        }
      }

      useEffectOnce {
        when (val config = config) {
          is AccessManagementDetailsConfig.Details -> initFields(config.accessManagement)
          is AccessManagementDetailsConfig.Edit -> initFields(config.accessManagement)
          is AccessManagementDetailsConfig.Create -> initFields(null)
        }

        fetchLocations()
      }

      return AccessManagementDetailsController(
        selectedLocation = selectedLocation,
        selectedLocationTextFieldError = selectedLocationTextFieldError,
        locationNameToLocationMap = locationNameToLocationMap,
        accessControlNoteTextFieldValue = accessControlNoteTextFieldValue,
        accessControlReasonTextFieldValue = accessControlReasonTextFieldValue,
        timeSlots = timeSlots,
        fromDateTimeSlotErrors = fromDateTimeSlotErrors,
        toDateTimeSlotErrors = toDateTimeSlotErrors,
        personEmailTextFieldValue = personEmailTextFieldValue,
        permittedPeopleList = permittedPeopleList,
        showProgress = showProgress,
        locationFetchInProgress = locationFetchInProgress,
        submitPermittedPeopleToState = ::submitPermittedPeopleToState,
        locationSelectionOnChange = locationSelectionOnChange,
        noteTextFieldOnChange = noteTextFieldOnChange,
        reasonTextFieldOnChange = reasonTextFieldOnChange,
        addTimeSlotOnClick = addTimeSlotOnClick,
        removeTimeSlotOnClick = ::removeTimeSlotOnClick,
        timeSlotDateFromOnChange = ::timeSlotDateFromOnChange,
        timeSlotTimeFromOnChange = ::timeSlotTimeFromOnChange,
        timeSlotDateToOnChange = ::timeSlotDateToOnChange,
        timeSlotTimeToOnChange = ::timeSlotTimeToOnChange,
        addPermittedPeopleOnChange = addPermittedPeopleOnChange,
        removePermittedPeopleOnClick = ::removePermittedPeopleOnClick,
        createAccessControlOnClick = ::createAccessControlOnClick,
      )
    }

    class TimeSlotError(val text: String, val timeSlot: ClientDateRange)
  }
}
package views.accessManagement.accessManagementDetails.timeSlotPicker

import com.studo.campusqr.common.payloads.ClientDateRange
import views.accessManagement.accessManagementDetails.AccessManagementDetailsConfig
import views.accessManagement.accessManagementDetails.AccessManagementDetailsController.Companion.TimeSlotError
import webcore.ButtonOnClick
import kotlin.js.Date

data class TimeSlotPickerConfig(
  val timeSlots: List<ClientDateRange>,
  val fromDateTimeSlotErrors: MutableList<TimeSlotError>,
  val toDateTimeSlotErrors: MutableList<TimeSlotError>,
  val accessManagementDetailsType: AccessManagementDetailsConfig,
  val addTimeSlotOnClick: ButtonOnClick,
  val removeTimeSlotOnClick: (clientDateRange: ClientDateRange) -> Unit,
  val timeSlotDateFromOnChange: (date: Date, clientDateRange: ClientDateRange, now: Date, inThreeYears: Date) -> Unit,
  val timeSlotTimeFromOnChange: (date: Date, clientDateRange: ClientDateRange) -> Unit,
  val timeSlotDateToOnChange: (date: Date, clientDateRange: ClientDateRange, now: Date, inThreeYears: Date) -> Unit,
  val timeSlotTimeToOnChange: (date: Date, clientDateRange: ClientDateRange) -> Unit,
)
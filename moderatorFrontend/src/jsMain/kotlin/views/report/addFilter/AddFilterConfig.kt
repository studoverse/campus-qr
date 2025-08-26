package views.report.addFilter

import com.studo.campusqr.common.payloads.ReportData
import react.RefObject
import webcore.MbDialogRef

class AddFilterConfig(
  val userLocation: ReportData.UserLocation,
  val dialogRef: RefObject<MbDialogRef>,
  val onApplyFilterChange: (userLocation: ReportData.UserLocation, filteredSeats: List<Int>) -> Unit,
)

package views.report.addFilter

import com.studo.campusqr.common.payloads.ReportData
import react.MutableRefObject
import webcore.MbDialogRef

class AddFilterConfig(
  val userLocation: ReportData.UserLocation,
  val dialogRef: MutableRefObject<MbDialogRef>,
  val onApplyFilterChange: (userLocation: ReportData.UserLocation, filteredSeats: List<Int>) -> Unit,
)

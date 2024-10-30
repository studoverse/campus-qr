package views.report.reportTableRow

import com.studo.campusqr.common.payloads.ReportData
import react.MutableRefObject
import webcore.MbDialogRef

class ReportTableRowConfig(
  val userLocation: ReportData.UserLocation,
  val showEmailAddress: Boolean,
  val dialogRef: MutableRefObject<MbDialogRef>,
  val onApplyFilterChange: (userLocation: ReportData.UserLocation, filteredSeats: List<Int>) -> Unit,
  val onDeleteFilter: (userLocation: ReportData.UserLocation) -> Unit
)

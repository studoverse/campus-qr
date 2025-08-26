package views.report.reportTableRow

import com.studo.campusqr.common.payloads.ReportData
import react.RefObject
import webcore.MbDialogRef

class ReportTableRowConfig(
  val userLocation: ReportData.UserLocation,
  val showEmailAddress: Boolean,
  val dialogRef: RefObject<MbDialogRef>,
  val onApplyFilterChange: (userLocation: ReportData.UserLocation, filteredSeats: List<Int>) -> Unit,
  val onDeleteFilter: (userLocation: ReportData.UserLocation) -> Unit
)

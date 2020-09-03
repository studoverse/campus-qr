package views.report

import com.studo.campusqr.common.ReportData
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import webcore.materialUI.mTableCell
import webcore.materialUI.mTableRow
import webcore.materialUI.withStyles

interface ReportTableRowProps : RProps {
  class Config(
      val userLocation: ReportData.UserLocation,
      val showEmailAddress: Boolean
  )

  var config: Config
  var classes: ReportTableRowClasses
}

interface ReportTableRowState : RState

class ReportTableRow : RComponent<ReportTableRowProps, ReportTableRowState>() {

  override fun RBuilder.render() {
    mTableRow {
      if (props.config.showEmailAddress) {
        mTableCell {
          +props.config.userLocation.email
        }
      }
      mTableCell {
        +props.config.userLocation.date
      }
      mTableCell {
        +props.config.userLocation.locationName
      }
    }
  }
}

interface ReportTableRowClasses {
  // Keep in sync with ReportItemStyle!
}

private val ReportTableRowStyle = { theme: dynamic ->
  // Keep in sync with ReportItemClasses!
}

private val styled = withStyles<ReportTableRowProps, ReportTableRow>(ReportTableRowStyle)

fun RBuilder.renderReportTableRow(config: ReportTableRowProps.Config) = styled {
  attrs.config = config
}
  
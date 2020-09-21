package views.report

import com.studo.campusqr.common.ReportData
import kotlinext.js.js
import react.*
import util.Strings
import util.get
import views.accessManagement.AccessManagementDetailsProps
import webcore.materialUI.*

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
      props.config.userLocation.seat?.let { locationSeatNumber ->
        mTableCell {
          +locationSeatNumber.toString()
        }
        mTableCell {
          muiAutocomplete {
            attrs.classes = js {
              root = props.classes.filter
            }
            attrs.onChange = { _, target: String?, _ ->
            }
            attrs.multiple = true
            attrs.openOnFocus = true
            attrs.options = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).map { it.toString() }.toTypedArray()
            attrs.getOptionLabel = { it }
            attrs.renderInput = { params: dynamic ->
              textField {
                attrs.id = params.id
                attrs.InputProps = params.InputProps
                attrs.inputProps = params.inputProps
                attrs.disabled = params.disabled
                attrs.fullWidth = params.fullWidth
                attrs.variant = "outlined"
                attrs.label = "Seat filter"
              }
            }
          }
        }
      }
    }
  }
}

interface ReportTableRowClasses {
  // Keep in sync with ReportItemStyle!
  var filter: String
}

private val ReportTableRowStyle = { theme: dynamic ->
  // Keep in sync with ReportItemClasses!
  js {
    filter = js {
      maxWidth = 300
    }
  }
}

private val styled = withStyles<ReportTableRowProps, ReportTableRow>(ReportTableRowStyle)

fun RBuilder.renderReportTableRow(config: ReportTableRowProps.Config) = styled {
  attrs.config = config
}
  